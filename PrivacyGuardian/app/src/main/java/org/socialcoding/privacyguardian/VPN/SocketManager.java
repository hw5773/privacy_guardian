package org.socialcoding.privacyguardian.VPN;

import android.os.StrictMode;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
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
    private int IP_HEADER_LENGTH = 20;
    private int TCP_HEADER_LENGTH = 20;
    private int UDP_HEADER_LENGTH = 8;
    private int MAX_BYTES = 1460;
    private int TIMING = 5;

    public SocketManager() {

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

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
                                Thread.sleep(TIMING);
                                continue;
                            }
                            System.out.println("Receive messages from " + tcpChannels + " TCP channels.");

                            if (tcpChannels > 0) {
                                Set<SelectionKey> selectedKeys = tcpSelector.selectedKeys();
                                System.out.println("Get the selectedKeys");
                                Iterator<SelectionKey> iter = selectedKeys.iterator();
                                System.out.println("Get the iterator");

                                while (iter.hasNext()) {
                                    SelectionKey key = iter.next();
                                    System.out.println("Get the key");
                                    SocketChannel socket = (SocketChannel) key.channel();
                                    System.out.println("Get the TCP Channel: " + socket.socket().getInetAddress() + ":" + socket.socket().getPort());
                                    ByteBuffer buf = ByteBuffer.allocate(MAX_BYTES);
                                    int bytes = 0, recv = 0;
                                    System.out.println("Let's read the Response");
                                    while (true) {
                                        recv = socket.read(buf);
                                        System.out.println("bytes: " + bytes + ", recv: " + recv);

                                        if (recv == -1) {
                                            System.out.println("Find EOF from the READ socket");
                                            break;
                                        } else if (bytes == 0 && recv == 0)
                                            break;
                                        else
                                            bytes += recv;

                                        if (bytes > 0) {
                                            System.out.println("Received " + bytes + " TCP bytes.");
                                            byte[] msg = new byte[bytes];
                                            System.arraycopy(buf.array(), 0, msg, 0, bytes);
                                            buf.clear();
                                            byte[] packet = makeTCPPacket(msg, socket.socket(), 1);
                                            addMessage(packet);
                                            tcpInfo.get(socket).setSeqNum(bytes);
                                            System.out.println("SEQ becomes " + tcpInfo.get(socket).getSeqNum());
                                            bytes = 0;
                                        }
                                    }

                                    if (bytes > 0) {
                                        System.out.println("Received " + bytes + " TCP bytes.");
                                        byte[] msg = new byte[bytes];
                                        System.arraycopy(buf.array(), 0, msg, 0, bytes);
                                        byte[] packet = makeTCPPacket(msg, socket.socket(), 1);
                                        addMessage(packet);
                                        tcpInfo.get(socket).setSeqNum(bytes);
                                        System.out.println("SEQ becomes " + tcpInfo.get(socket).getSeqNum());
                                    }

                                    // Generate FIN packet
                                    if (!socket.isOpen()) {
                                        System.out.println("Send the FIN packet to " + socket.socket().getInetAddress() + ":" + socket.socket().getPort());
                                        byte[] fin = makeTCPPacket(null, socket.socket(), 2);
                                        addMessage(fin);
                                        Thread.sleep(10000);
                                    }
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
                                Thread.sleep(TIMING);
                                continue;
                            }
                            System.out.println("Receive messages from " + udpChannels + " UDP channels.");

                            if (udpChannels > 0) {
                                Set<SelectionKey> selectedKeys = udpSelector.selectedKeys();
                                Iterator<SelectionKey> iter = selectedKeys.iterator();

                                while (iter.hasNext()) {
                                    SelectionKey key = iter.next();
                                    DatagramChannel socket = (DatagramChannel) key.channel();
                                    ByteBuffer buf = ByteBuffer.allocate(MAX_BYTES);
                                    int bytes = 0, recv;
                                    while (true) {
                                        recv = socket.read(buf);
                                        if (bytes > 0 && recv == 0)
                                            break;
                                        bytes += recv;
                                    }
                                    System.out.println("Received " + bytes + " UDP bytes from " + socket.socket().getInetAddress() + ":" + socket.socket().getPort());
                                    byte[] msg = new byte[bytes];
                                    System.arraycopy(buf.array(), 0, msg, 0, bytes);
                                    byte[] packet = makeUDPPacket(msg, socket.socket());
                                    addMessage(packet);
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

    private boolean validChecksum(byte[] packet) {
        int result = 0;
        boolean ret = false;

        for (int i=0; i<packet.length; i+=2) {
            result += (int)(((packet[i] & 0xff) << 8) | (packet[i+1] & 0xff));
        }

        for (int i=0; i<2; i++) {
            result = ((result & 0xffff0000) >> 16) + (result & 0xffff);
        }

        result = (~result) & 0xffff;

        if (result == 0) {
            System.out.println("Result: " + result);
            ret = true;
        } else {
            System.out.println("Result: " + result);
            ret = false;
        }

        return ret;
    }

    public boolean checkSocket(boolean isTCP, String clntIP, int clntPort) {
        String key = makeKey(clntIP, clntPort);
        boolean ret = false;

        if (isTCP) {
            if (tcpSock.containsKey(key))
                ret = true;
        } else {
            if (udpSock.containsKey(key))
                ret = true;
        }

        return ret;
    }

    // Add the TCP Socket into the manager
    @Override
    public void addTCPSocket(SocketChannel socket, IPHeader ipHdr, TCPHeader tcpHdr) {
        System.out.println("TCP packet is added to SocketManager.");
        try {
            // Key is the combination of the client IP address and the client port
            // VPN will give the SYN/ACK packet. So the destination is the client
            String key = makeKey(ipHdr.getSourceIP(), tcpHdr.getSourcePort());
            System.out.println("TCP is selected");
            System.out.println("key: " + key);
            socket.configureBlocking(false); // Set the socket in non-blocking mode
            // Connect to the server
            socket.connect(new InetSocketAddress(ipHdr.getDestIP(), tcpHdr.getDestPort()));
            while (!socket.finishConnect()); // Wait until finishing TCP handshake

            // Log message
            System.out.println("Complete to make the socket with " + makeKey(ipHdr.getDestIP(), tcpHdr.getDestPort()));
            System.out.println("Socket in addSocket: " + socket);

            tcpSelector.wakeup();
            socket.register(tcpSelector, SelectionKey.OP_READ, null);
            System.out.println("Socket is registered in the Selector");
            System.out.println("ACK Number in addTCPSocket before set: " + tcpHdr.getSequenceNumber());
            TCPSocketInfo info = new TCPSocketInfo(socket, ipHdr.getSourceIP(), tcpHdr.getSourcePort(), tcpHdr.getSequenceNumber(), tcpHdr.getAckNumber());
            System.out.println("ACK Number in addTCPSocket after set: " + info.getAckNum());
            info.setSeqNum(1);

            // Input the information and the socket into the appropriate table
            tcpInfo.put(socket, info);
            tcpInfoByPort.put(socket.socket().getLocalPort(), info);
            tcpSock.put(key, socket);
        }
        catch (IOException e)
        {
            System.out.println("Socket is not generated well");
            System.out.println(e.getStackTrace());
        }
    }

    // Add the TCP Socket into the manager
    @Override
    public void addTLSSocket(SocketChannel socket, IPHeader ipHdr, TCPHeader tcpHdr) {
        System.out.println("TLS packet is added to SocketManager.");
        try {
            // Key is the combination of the client IP address and the client port
            // VPN will give the SYN/ACK packet. So the destination is the client
            String key = makeKey(ipHdr.getSourceIP(), tcpHdr.getSourcePort());
            System.out.println("TLS is selected");
            System.out.println("key: " + key);
            socket.configureBlocking(false); // Set the socket in non-blocking mode
            // Connect to the server
            socket.connect(new InetSocketAddress(ipHdr.getDestIP(), tcpHdr.getDestPort()));
            while (!socket.finishConnect()); // Wait until finishing TCP handshake

            // Log message
            System.out.println("Complete to make the socket with " + makeKey(ipHdr.getDestIP(), tcpHdr.getDestPort()));
            System.out.println("Socket in addSocket: " + socket);

            tcpSelector.wakeup();
            socket.register(tcpSelector, SelectionKey.OP_READ, null);
            System.out.println("Socket is registered in the Selector");
            System.out.println("ACK Number in addTCPSocket before set: " + tcpHdr.getSequenceNumber());
            TCPSocketInfo info = new TCPSocketInfo(socket, ipHdr.getSourceIP(), tcpHdr.getSourcePort(), tcpHdr.getSequenceNumber(), tcpHdr.getAckNumber());
            System.out.println("ACK Number in addTCPSocket after set: " + info.getAckNum());
            info.setSeqNum(1);

            // Input the information and the socket into the appropriate table
            tcpInfo.put(socket, info);
            tcpInfoByPort.put(socket.socket().getLocalPort(), info);
            tcpSock.put(key, socket);
        }
        catch (IOException e)
        {
            System.out.println("Socket is not generated well");
            System.out.println(e.getStackTrace());
        }
    }

    // Add the UDP socket into the manager
    @Override
    public void addUDPSocket(DatagramChannel socket, IPHeader ipHdr, UDPHeader udpHdr) {
        System.out.println("UDP packet is added to SocketManager.");
        try {
            // Key is composed of Client IP and Client Port
            String key = makeKey(ipHdr.getSourceIP(), udpHdr.getSourcePort());
            System.out.println("Key: " + key);
            socket.configureBlocking(false);
            socket.connect(new InetSocketAddress(ipHdr.getDestIP(), udpHdr.getDestPort()));
            while (!socket.isConnected());
            System.out.println("This socket is connected to " + makeKey(ipHdr.getDestIP(), udpHdr.getDestPort()));
            udpSelector.wakeup();
            socket.register(udpSelector, SelectionKey.OP_READ, null);
            UDPSocketInfo info = new UDPSocketInfo(socket, ipHdr.getSourceIP(), udpHdr.getSourcePort());

            udpInfo.put(socket, info);
            udpInfoByPort.put(socket.socket().getLocalPort(), info);
            udpSock.put(key, socket);
        }
        catch (IOException e)
        {
            System.out.println("Socket is not generated well");
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
        if (tcpSock.containsKey(key)) {
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
    }

    // Delete the UDP socket from the manager
    private void delUDPSocket(String key) {
        if (udpSock.containsKey(key)) {
            try {
                DatagramChannel tmp = udpSock.get(key);
                udpInfo.remove(tmp);
                tcpSock.remove(key);
                tmp.close();
            } catch (IOException e) {
                System.out.println(e.getStackTrace());
            }
        }
    }

    @Override
    public void sendMessage(boolean isTCP, String clntIP, int clntPort, byte[] payload) {
        String key = makeKey(clntIP, clntPort);
        ByteBuffer msg = ByteBuffer.wrap(payload);

        if (isTCP) {
            sendTCPMessage(key, msg, payload.length);
        } else {
            sendUDPMessage(key, msg);
        }
    }

    // Send the message with the TCP socket
    private void sendTCPMessage(String key, ByteBuffer msg, int size) {
        try {
            System.out.println("Send the message from " + key);
            int bytes = 0;
            if (tcpSock.containsKey(key)) {
                TCPSocketInfo info = tcpInfo.get(tcpSock.get(key));
                SocketChannel socket = info.getSocket();
                bytes = socket.write(msg);
                System.out.println("ACK in sendTCPMessage before: " + info.getAckNum() + " " + key);
                info.setAckNum(bytes);
                System.out.println("Send TCP " + bytes + " bytes " + key);
                System.out.println("ACK in sendTCPMessage after: " + info.getAckNum() + " " + key);
                byte[] ack = makeTCPPacket(null, socket.socket(), 0);
                addMessage(ack);
            } else {
                System.out.println("Socket is not found in tcpSock " + key);
            }
        }
        catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
    }

    // Send the message with the UDP socket
    private void sendUDPMessage(String key, ByteBuffer msg) {
        try {
            System.out.println("Send the message from " + key);
            int bytes = 0;
            if (udpSock.containsKey(key)) {
                bytes = udpSock.get(key).write(msg);
            }
            else {
                System.out.println("Socket is not found in udpSock with " + key);
            }
        }
        catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xff;
            hexChars[j * 3] = hexArray[ v >>> 4 ];
            hexChars[j * 3 + 1] = hexArray[ v & 0x0f ];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }

    @Override
    public boolean isMessage() {
        return !(msgQueue.isEmpty());
    }

    @Override
    public byte[] getMessage() {
        return msgQueue.poll();
    }


    // Make the TCP packet
    // Flags: 0 (ACK), 1 (PSH/ACK), 2 (FIN/ACK)
    private byte[] makeTCPPacket(byte[] msg, Socket socket, int flags) {
        int msgLength = 0;
        if (msg != null) {
            System.out.println("Make TCP Packet: " + msg.length);
            msgLength = msg.length;
        } else {
            System.out.println("Send the ACK Packet");
        }

        byte[] tcp = new byte[TCP_HEADER_LENGTH];
        byte[] ip = new byte[IP_HEADER_LENGTH];
        int totalLength = msgLength + tcp.length + ip.length;
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

        info.setSeqNum(msgLength);

        IPHeader ipH = new IPHeader();
        ipH.setTotalLength(totalLength);
        ipH.setIdentifier(id);
        ipH.setSourceIP(servAddr);
        ipH.setDestIP(clntAddr);
        ipH.setTCP();
        int ipChecksum = makeIPChecksum(ipH.getHeader());
        ipH.setChecksum(ipChecksum);
        ip = ipH.getHeader();

        System.out.println("Verify IP Checksum: " + validChecksum(ip));

        TCPHeader tcpH = new TCPHeader();
        tcpH.setSourcePort(servPort);
        tcpH.setDestPort(clntPort);
        tcpH.setSequenceNumber(seq);
        tcpH.setAckNumber(ack);
        tcpH.setFlag(flags);

        System.out.println("RESP- SEQ from Server: " + tcpH.getSequenceNumber() + " " + ipH.getDestIP() + ":" + tcpH.getDestPort());
        System.out.println("RESP- ACK from Server: " + tcpH.getAckNumber() + " " + ipH.getDestIP() + ":" + tcpH.getDestPort());

        int tcpChecksum = makeTCPChecksum(tcpH.getHeader(), ip, msg);
        tcpH.setChecksum(tcpChecksum);
        tcp = tcpH.getHeader();

        System.arraycopy(ip, 0, packet, 0, ip.length);
        System.arraycopy(tcp, 0, packet, ip.length, tcp.length);

        if (msg != null)
            System.arraycopy(msg, 0, packet, ip.length + tcp.length, msgLength);

        return packet;
    }

    // Make the UDP packet
    private byte[] makeUDPPacket(byte[] msg, DatagramSocket socket) {
        byte[] udp = new byte[UDP_HEADER_LENGTH];
        byte[] ip = new byte[IP_HEADER_LENGTH];
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
        System.out.println("Client IP Address: " + clntAddr + ":" + clntPort);
        System.out.println("Server IP Address: " + servAddr + ":" + servPort);
        System.out.println("TotalLength: " + totalLength);

        // Generating the IP header
        IPHeader ipH = new IPHeader();
        ipH.setTotalLength(totalLength);
        ipH.setIdentifier(id);
        ipH.setSourceIP(servAddr);
        ipH.setDestIP(clntAddr);
        ipH.setUDP();
        int ipChecksum = makeIPChecksum(ipH.getHeader());
        ipH.setChecksum(ipChecksum);
        ip = ipH.getHeader();

        System.out.println("Verify IP Checksum: " + validChecksum(ip));

        // Generating the UDP header
        UDPHeader udpH = new UDPHeader();
        udpH.setSourcePort(servPort);
        udpH.setDestPort(clntPort);
        udpH.setLength(length);
        int udpChecksum = makeUDPChecksum(udpH.getHeader(), ip, msg);
        udpH.setChecksum(udpChecksum);
        udp = udpH.getHeader();

        System.out.println("IP Header: " + bytesToHex(ip));
        System.out.println("UDP Header: " + bytesToHex(udp));
        System.out.println("MSG: " + bytesToHex(msg));

        System.arraycopy(ip, 0, packet, 0, ip.length);
        System.arraycopy(udp, 0, packet, ip.length, udp.length);
        System.arraycopy(msg, 0, packet, ip.length + udp.length, msg.length);

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
        int msgLen = 0;

        if (msg != null)
            msgLen = msg.length;

        int offset = ((tcpHdr[12] & 0xf0) >> 4) * 4;

        byte[] pseudoHdr = new byte[tcpLen + 12 + msgLen];
        System.arraycopy(ipHdr, 12, pseudoHdr, 0, 8);
        System.arraycopy(tcpHdr, 0, pseudoHdr, 12, tcpLen);

        if (msg != null)
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

        int length = ((udpHdr[4] & 0xff) << 8) | (udpHdr[5] & 0xff);

        byte[] pseudoHdr = new byte[udpLen + 12 + msgLen];
        System.arraycopy(ipHdr, 12, pseudoHdr, 0, 8);
        System.arraycopy(udpHdr, 0, pseudoHdr, 12, udpLen);
        System.arraycopy(msg, 0, pseudoHdr, udpLen + 12, msgLen);
        pseudoHdr[8] = (byte) 0x0;
        pseudoHdr[9] = (byte) 0x11;
        pseudoHdr[10] = (byte) ((length & 0xff00) >> 8);
        pseudoHdr[11] = (byte) (length & 0xff);

        return makeChecksum(pseudoHdr);
    }

    // Add the message in the message queue
    private void addMessage(byte[] msg) {
        try {
            System.out.println("Input the message into the MsgQueue: " + msg.length);
            msgQueue.put(msg);
        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace());
        }
    }

    private String makeKey(String sourceIP, int sourcePort) {
        return sourceIP + ":" + sourcePort;
    }

}
