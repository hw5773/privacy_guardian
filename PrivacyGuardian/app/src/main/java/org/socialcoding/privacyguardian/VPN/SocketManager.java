package org.socialcoding.privacyguardian.VPN;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Hyunwoo Lee on 2017-02-12.
 */

public class SocketManager implements SocketManagerAPI {

    private Selector tcpSelector;
    private Selector udpSelector;

    private Hashtable<SocketChannel, TCPSocketInfo> tcpInfo;
    private Hashtable<DatagramChannel, UDPSocketInfo> udpInfo;
    private Hashtable<Integer, TCPSocketInfo> tcpInfoByPort;
    private Hashtable<Integer, UDPSocketInfo> udpInfoByPort;

    private Hashtable<String, SocketChannel> tcpSock;
    private Hashtable<String, DatagramChannel> udpSock;

    private LinkedBlockingQueue<byte[]> msgQueue;
    private Thread tcpThread;
    private Thread udpThread;
    private int IPHeaderLength = 20;
    private int TCPHeaderLength = 20;
    private int UDPHeaderLength = 20;

    public SocketManager() {
        try {
            tcpSelector = Selector.open();
            udpSelector = Selector.open();
            tcpInfo = new Hashtable<SocketChannel, TCPSocketInfo>();
            udpInfo = new Hashtable<DatagramChannel, UDPSocketInfo>();
            tcpSock = new Hashtable<String, SocketChannel>();
            udpSock = new Hashtable<String, DatagramChannel>();
            tcpInfoByPort = new Hashtable<Integer, TCPSocketInfo>();
            udpInfoByPort = new Hashtable<Integer, UDPSocketInfo>();
            msgQueue = new LinkedBlockingQueue<byte[]>();

            tcpThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    System.out.println("TCP Poller starts.");
                    for(;;) {
                        try {
                            int tcpChannels = tcpSelector.select();

                            if (tcpChannels == 0) {
                                Thread.sleep(100);
                                continue;
                            }
                            System.out.println("Receive messages from " + tcpChannels + " TCP channels.");

                            if (tcpChannels > 0) {
                                Set<SelectionKey> selectedKeys = tcpSelector.selectedKeys();
                                Iterator<SelectionKey> iter = selectedKeys.iterator();

                                while (iter.hasNext()) {
                                    SelectionKey key = iter.next();
                                    SocketChannel socket = (SocketChannel) key.channel();
                                    ByteBuffer buf = ByteBuffer.allocate(256);
                                    socket.read(buf);
                                    makeMessage(true, buf.array(), socket.socket());
                                }
                            }
                        } catch (IOException e) {
                            System.out.println(e.getStackTrace());
                        } catch (InterruptedException e) {
                            System.out.println(e.getStackTrace());
                        }
                    }
                }

            }, "TCPPoller");

            udpThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    System.out.println("UDP Poller starts.");
                    for(;;) {
                        try {
                            int udpChannels = udpSelector.select();

                            if (udpChannels == 0) {
                                Thread.sleep(100);
                                continue;
                            }
                            System.out.println("Receive messages from " + udpChannels + " UDP channels.");

                            if (udpChannels > 0) {
                                Set<SelectionKey> selectedKeys = udpSelector.selectedKeys();
                                Iterator<SelectionKey> iter = selectedKeys.iterator();

                                while (iter.hasNext()) {
                                    SelectionKey key = iter.next();
                                    SocketChannel socket = (SocketChannel) key.channel();
                                    ByteBuffer buf = ByteBuffer.allocate(256);
                                    socket.read(buf);
                                    makeMessage(true, buf.array(), socket.socket());
                                }
                            }
                        } catch (IOException e) {
                            System.out.println(e.getStackTrace());
                        } catch (InterruptedException e) {
                            System.out.println(e.getStackTrace());
                        }
                    }
                }

            }, "UDPPoller");

        }
        catch (Exception e)
        {
            System.out.println(e.getStackTrace());
        }
        tcpThread.start();
        udpThread.start();
    }

    @Override
    public void addSocket(boolean isTCP, IP_Header ipHdr, TransmissionHeader tHdr) {

        if (isTCP) {
            TCP_Header tcpHdr = (TCP_Header) tHdr;
            addTCPSocket(ipHdr, tcpHdr);
        } else {
            UDP_Header udpHdr = (UDP_Header) tHdr;
            addUDPSocket(ipHdr, udpHdr);
        }
    }

    // Add the TCP Socket into the manager
    private void addTCPSocket(IP_Header ipHdr, TCP_Header tcpHdr) {
        try {
            // Key is the combination of the client IP address and the client port
            // VPN will give the SYN/ACK packet. So the destination is the client
            String key = makeKey(ipHdr.getDestIP(), tcpHdr.getDestPort());
            System.out.println("TCP is selected");
            System.out.println("key: " + key);
            SocketChannel socket = SocketChannel.open();
            socket.configureBlocking(false); // Set the socket in non-blocking mode
            // Connect to the server
            socket.connect(new InetSocketAddress(ipHdr.getSourceIP(), tcpHdr.getSourcePort()));
            while (!socket.finishConnect()); // Wait until finishing TCP handshake

            // Log message
            System.out.println("Complete to make the socket with " + makeKey(ipHdr.getSourceIP(), tcpHdr.getSourcePort()));
            System.out.println("Socket in addSocket: " + socket);

            tcpSelector.wakeup();
            socket.register(tcpSelector, SelectionKey.OP_READ, null);
            System.out.println("Socket is registered in the Selector");
            TCPSocketInfo info = new TCPSocketInfo(socket, ipHdr.getDestIP(), tcpHdr.getDestPort(), tcpHdr.getSequenceNumber(), tcpHdr.getAckNumber());
            info.setSeqNum(1);

            // Input the information and the socket into the appropriate table
            tcpInfo.put(socket, info);
            tcpInfoByPort.put(socket.socket().getLocalPort(), info);
            tcpSock.put(key, socket);

            System.out.println("Socket is inputted into the TCP hash table");
        }
        catch (IOException e)
        {
            System.out.println(e.getStackTrace());
        }
    }

    // Add the UDP socket into the manager
    private void addUDPSocket(IP_Header ipHdr, UDP_Header udpHdr) {
        try {
            // Key is composed of Client IP and Client Port
            String key = makeKey(ipHdr.getDestIP(), udpHdr.getDestPort());
            System.out.println("key: " + key);
            DatagramChannel socket = DatagramChannel.open();
            socket.configureBlocking(false);
            socket.connect(new InetSocketAddress(ipHdr.getSourceIP(), udpHdr.getSourcePort()));
            udpSelector.wakeup();
            socket.register(udpSelector, SelectionKey.OP_READ, null);
            UDPSocketInfo info = new UDPSocketInfo(socket, ipHdr.getDestIP(), udpHdr.getDestPort());

            udpInfo.put(socket, info);
            udpInfoByPort.put(socket.socket().getLocalPort(), info);
            udpSock.put(key, socket);

        }
        catch (IOException e)
        {
            System.out.println(e.getStackTrace());
        }
    }

    @Override
    public void delSocket(boolean isTCP, String clntIP, int clntPort) {
        String key = makeKey(clntIP, clntPort);

        if (isTCP) {
            delTCPSocket(key);
        } else {
            delUDPSocket(key);
        }
    }

    // Delete the TCP socket from the manager
    private void delTCPSocket(String key) {
        try {
            SocketChannel tmp = tcpSock.get(key);
            tcpInfo.remove(tmp);
            tcpSock.remove(key);
            tmp.close();
            System.out.println("Now Socket is deleted.");
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
    }

    // Delete the UDP socket from the manager
    private void delUDPSocket(String key) {
        try {
            DatagramChannel tmp = udpSock.get(key);
            udpInfo.remove(tmp);
            tcpSock.remove(key);
            tmp.close();
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
    }

    @Override
    public void sendMessage(boolean isTCP, String clntIP, int clntPort, String payload) {
        String key = makeKey(clntIP, clntPort);
        ByteBuffer msg = ByteBuffer.wrap(payload.getBytes());

        if (isTCP)
        {
            sendTCPMessage(key, msg, payload.length());
        } else {
            sendUDPMessage(key, msg);
        }
    }

    // Send the message with the TCP socket
    private void sendTCPMessage(String key, ByteBuffer msg, int size) {
        try
        {
            SocketChannel a = tcpSock.get(key);
            TCPSocketInfo info = tcpInfo.get(a);
            info.getSocket().write(msg);
            info.setAckNum(size);
        }
        catch (IOException e)
        {
            System.out.println(e.getStackTrace());
        }
    }

    // Send the message with the UDP socket
    private void sendUDPMessage(String key, ByteBuffer msg) {
        try
        {
            udpSock.get(key).write(msg);
        }
        catch (IOException e)
        {
            System.out.println(e.getStackTrace());
        }
    }

    @Override
    public boolean isMessage() {
        return !(msgQueue.isEmpty());
    }

    @Override
    public byte[] getMessage() {
        return msgQueue.poll();
    }

    // Make the packet and add it into the message queue
    private void makeMessage(boolean isTCP, byte[] msg, Socket socket) {
        byte[] packet;

        if (isTCP)
            packet = makeTCPPacket(msg, socket);
        else
            packet = makeUDPPacket(msg, socket);

        addMessage(packet);
    }

    // Make the TCP packet
    private byte[] makeTCPPacket(byte[] msg, Socket socket) {
        byte[] tcp = new byte[TCPHeaderLength];
        byte[] ip = new byte[IPHeaderLength];
        int totalLength = msg.length + tcp.length + ip.length;
        byte[] packet = new byte[totalLength];

        TCPSocketInfo info = tcpInfoByPort.get(socket.getLocalPort());
        String clntAddr = info.getClntIPAddr();
        int clntPort = info.getClntPort();
        String servAddr = socket.getInetAddress().getHostAddress().trim();
        int servPort = socket.getPort();

        byte[] serv, clnt;
        long seq = info.getSeqNum(); // Get the sequence number
        long ack = info.getAckNum(); // Get the acknowledge number
        int id = info.getIdentification(); // Get the identification field number

        System.out.println("Source Addr in make: " + makeKey(servAddr, servPort));
        System.out.println("Dest Addr in make: " + makeKey(clntAddr, clntPort));

        info.setSeqNum(msg.length);

        try {
            serv = InetAddress.getByName(servAddr).getAddress();
            clnt = InetAddress.getByName(clntAddr).getAddress();

            ip[0] = (byte) 0x45;
            ip[1] = (byte) 0x00;
            ip[2] = (byte) ((totalLength & 0xff00) >> 8);
            ip[3] = (byte) (totalLength & 0xff);
            ip[4] = (byte) ((id & 0xff00) >> 8);
            ip[5] = (byte) (id & 0xff);
            ip[6] = (byte) 0x00;
            ip[7] = (byte) 0x00;
            ip[8] = (byte) 0x40;
            ip[9] = (byte) 0x06;
            ip[10] = (byte) 0;
            ip[11] = (byte) 0;
            ip[12] = serv[0];
            ip[13] = serv[1];
            ip[14] = serv[2];
            ip[15] = serv[3];
            ip[16] = clnt[0];
            ip[17] = clnt[1];
            ip[18] = clnt[2];
            ip[19] = clnt[3];

            int ipChecksum = makeIPChecksum(ip);
            ip[10] = (byte) ((ipChecksum & 0xff00) >> 8);
            ip[11] = (byte) (ipChecksum &0xff);

            tcp[0] = (byte) ((servPort & 0xff00) >> 8);
            tcp[1] = (byte) (servPort & 0xff);
            System.out.println("TCP servPort: " + servPort);
            System.out.println("TCP servPort in packet: " + (((tcp[0] & 0xff) << 8) | (tcp[1] & 0xff)));
            tcp[2] = (byte) ((clntPort & 0xff00) >> 8);
            tcp[3] = (byte) (clntPort & 0xff);
            System.out.println("TCP clntPort: " + clntPort);
            System.out.println("TCP clntPort in packet: " + (((tcp[2] & 0xff) << 8) | (tcp[3] & 0xff)));
            tcp[4] = (byte) ((seq & 0xff000000) >> 24);
            tcp[5] = (byte) ((seq & 0xff0000) >> 16);
            tcp[6] = (byte) ((seq & 0xff00) >> 8);
            tcp[7] = (byte) (seq & 0xff);
            tcp[8] = (byte) ((ack & 0xff000000) >> 24);
            tcp[9] = (byte) ((ack & 0xff0000) >> 16);
            tcp[10] = (byte) ((ack & 0xff00) >> 8);
            tcp[11] = (byte) (ack & 0xff);
            tcp[12] = (byte) 0x50;
            tcp[13] = (byte) 16;
            tcp[14] = (byte) ((1000 & 0xff00) >> 8);
            tcp[15] = (byte) (1000 & 0xff);
            tcp[16] = (byte) 0;
            tcp[17] = (byte) 0;
            tcp[18] = (byte) 0;
            tcp[19] = (byte) 0;

            int tcpChecksum = makeTCPChecksum(tcp, ip, msg);
            tcp[16] = (byte) ((tcpChecksum & 0xff00) >> 8);
            tcp[17] = (byte) (tcpChecksum & 0xff);

            System.arraycopy(ip, 0, packet, 0, ip.length);
            System.arraycopy(tcp, 0, packet, ip.length, tcp.length);
            System.arraycopy(msg, 0, packet, ip.length + tcp.length, msg.length);

        } catch (UnknownHostException e) {
            System.out.println(e.getStackTrace());
        }

        return packet;
    }

    // Make the UDP packet
    private byte[] makeUDPPacket(byte[] msg, Socket socket) {
        byte[] udp = new byte[UDPHeaderLength];
        byte[] ip = new byte[IPHeaderLength];
        int totalLength = msg.length + udp.length + ip.length;
        byte[] packet = new byte[totalLength];
        UDPSocketInfo info = udpInfoByPort.get(socket.getLocalPort());
        String clntAddr = info.getClntIPAddr();
        int clntPort = info.getClntPort();
        String servAddr = socket.getInetAddress().getHostAddress();
        int servPort = socket.getPort();
        int id = info.getIdentification();
        int length = udp.length + msg.length;

        System.out.println("Making the UDP Packet");

        byte[] serv, clnt;

        try {
            serv = InetAddress.getByName(servAddr).getAddress();
            clnt = InetAddress.getByName(clntAddr).getAddress();

            ip[0] = (byte) 0x45;
            ip[1] = (byte) 0x00;
            ip[2] = (byte) ((totalLength & 0xff00) >> 8);
            ip[3] = (byte) (totalLength & 0xff);
            ip[4] = (byte) ((id & 0xff00) >> 8);
            ip[5] = (byte) (id & 0xff);
            ip[6] = (byte) 0x00;
            ip[7] = (byte) 0x00;
            ip[8] = (byte) 0x40;
            ip[9] = (byte) 0x11;
            ip[10] = (byte) 0;
            ip[11] = (byte) 0;
            ip[12] = serv[0];
            ip[13] = serv[1];
            ip[14] = serv[2];
            ip[15] = serv[3];
            ip[16] = clnt[0];
            ip[17] = clnt[1];
            ip[18] = clnt[2];
            ip[19] = clnt[3];

            int ipChecksum = makeIPChecksum(ip);
            ip[10] = (byte) ((ipChecksum & 0xff00) >> 8);
            ip[11] = (byte) (ipChecksum &0xff);

            udp[0] = (byte) ((servPort & 0xff00) >> 8);
            udp[1] = (byte) (servPort & 0xff);
            System.out.println("udp servPort: " + servPort);
            System.out.println("udp servPort in packet: " + (((udp[0] & 0xff) << 8) | (udp[1] & 0xff)));
            udp[2] = (byte) ((clntPort & 0xff00) >> 8);
            udp[3] = (byte) (clntPort & 0xff);
            System.out.println("udp clntPort: " + clntPort);
            System.out.println("udp clntPort in packet: " + (((udp[2] & 0xff) << 8) | (udp[3] & 0xff)));

            udp[4] = (byte) ((length & 0xff00) >> 8);
            udp[5] = (byte) (length & 0xff);

            int udpChecksum = makeUDPChecksum(udp, ip, msg);
            udp[6] = (byte) ((udpChecksum & 0xff00) >> 8);
            udp[7] = (byte) (udpChecksum & 0xff);

            System.arraycopy(ip, 0, packet, 0, ip.length);
            System.arraycopy(udp, 0, packet, ip.length, udp.length);
            System.arraycopy(msg, 0, packet, ip.length + udp.length, msg.length);

        } catch (UnknownHostException e) {
            System.out.println(e.getStackTrace());
        }

        return packet;
    }

    // Make the checksum
    private int makeChecksum(byte[] header){
        int length = header.length;
        if(length == 0){
            return 1;
        }
        int checksum  = 0;
        for(int i=0; i<length ; i+=2){
            int tmp = header[i];
            if( (i+1) >= length) { //odd
                checksum  = checksum +(int) ((tmp&0xff)<<8);
            }
            else {
                int tmp2 = header[i+1];
                checksum  = checksum + (int) (((tmp&0xff) << 8)|(tmp2&0xff));
            }
        }
        checksum =((checksum & 0xffff) + (checksum >> 16));
        return ~checksum ;
    }

    // Make the IP checksum
    private int makeIPChecksum(byte[] ipHdr) {
        return makeChecksum(ipHdr);
    }

    // Make the TCP checksum
    private int makeTCPChecksum(byte[] tcpHdr, byte[] ipHdr, byte[] msg) {
        int tcpLen = tcpHdr.length;
        int msgLen = msg.length;

        int offset = ((tcpHdr[12] & 0xf0) >> 4) * 4;

        byte[] pseudoHdr = new byte[tcpLen + 12 + msgLen];
        System.arraycopy(ipHdr, 12, pseudoHdr, 0, 8);
        System.arraycopy(tcpHdr, 0, pseudoHdr, 12, tcpLen);
        System.arraycopy(msg, 0, pseudoHdr, tcpLen + 12, msgLen);
        pseudoHdr[8] = (byte) 0x0;
        pseudoHdr[9] = (byte) 0x6;
        pseudoHdr[10] = (byte) (((offset + msgLen) & 0xff00) >> 8);
        pseudoHdr[11] = (byte) ((offset + msgLen) & 0xff);

        return makeChecksum(pseudoHdr);
    }

    // Make the UDP checksum
    private int makeUDPChecksum(byte[] udpHdr, byte[] ipHdr, byte[] msg) {
        int udpLen = udpHdr.length;
        int msgLen = msg.length;

        int offset = ((udpHdr[4] & 0xff) << 8) | (udpHdr[5] & 0xff);

        byte[] pseudoHdr = new byte[udpLen + 12 + msgLen];
        System.arraycopy(ipHdr, 12, pseudoHdr, 0, 8);
        System.arraycopy(udpHdr, 0, pseudoHdr, 12, udpLen);
        System.arraycopy(msg, 0, pseudoHdr, udpLen + 12, msgLen);
        pseudoHdr[8] = (byte) 0x0;
        pseudoHdr[9] = (byte) 0x11;
        pseudoHdr[10] = (byte) (((offset + msgLen) & 0xff00) >> 8);
        pseudoHdr[11] = (byte) ((offset + msgLen) & 0xff);

        return makeChecksum(pseudoHdr);
    }

    // Add the message in the message queue
    private void addMessage(byte[] msg) {
        try {
            msgQueue.put(msg);
        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace());
        }
    }

    private String makeKey(String ip, int port)
    {
        return ip + ":" + port;
    }

}
