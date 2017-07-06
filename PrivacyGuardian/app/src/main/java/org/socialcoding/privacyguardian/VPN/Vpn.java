package org.socialcoding.privacyguardian.VPN;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import org.socialcoding.privacyguardian.Activity.MainActivity;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by user on 2016-08-14.
 */
public class Vpn extends VpnService {
    private static final String TAG = "VpnServiceTest";
    private Thread mThread;
    private ParcelFileDescriptor mInterface;
    Builder builder = new Builder();
    private Context mContext = null;
    private static int TIMING = 5;
    private int MAX_BYTES = 1460;
    private int UDP_OFFSET = 8;

    private boolean mIsRunning;

    //VPN SERVICE SECTION START

    Messenger mainMessenger;

    public static final int REGISTER = 1000;
    public static final int SENDPAYLOAD = 1001;
    public static final int ENDVPN = 1002;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case REGISTER:
                    Log.d("INCOMING", "GOT REGISTER");
                    mainMessenger = msg.replyTo;
                    break;
                case ENDVPN:
                    Log.d("INCOMING", "GOT END SIGNAL");
                    try {
                        mInterface.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mIsRunning = false;
                    stopSelf();
                    Log.d("INCOMING", "ENDED");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    public int onStartCommand(final Intent intent, int flags, int startId) {
        mContext = getApplicationContext();
        mIsRunning = true;
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mInterface = builder.setSession("MyVPNService")
                         .addAddress("192.168.0.1", 24)
                         .addDnsServer("8.8.8.8")
                         .addRoute("0.0.0.0", 0).establish();
                    FileInputStream in = new FileInputStream(mInterface.getFileDescriptor());
                    FileOutputStream out = new FileOutputStream(mInterface.getFileDescriptor());
                    System.out.println("Start the SocketManager.");
                    SocketManager socketManager = new SocketManager();

                    int length;                                                        //length of packet.
                    ByteBuffer packet = ByteBuffer.allocate(MAX_BYTES);

                    while (mIsRunning) {
                        // Send the message to the client applications, if any.
                        while (socketManager.isMessage()) {
                            System.out.println("Found the Message");
                            byte[] msg = socketManager.getMessage();
                            System.out.println("Msg Length: " + msg.length);
                            out.write(msg);
                            System.out.println("Send the message to TUN");
                        }

                        // Read the message from the TUN
                        length = in.read(packet.array());
                        if (length > 0) {
                            packet.limit(length);
                            byte[] tmpPacket = new byte[length];
                            System.arraycopy(packet.array(), 0, tmpPacket, 0, length);
                            packet.clear();
                            int ihl = getIhl(tmpPacket);
                            System.out.println("IP Header Length: " + ihl);

                            // Parse the IP Header
                            IPHeader ipHeader = new IPHeader(tmpPacket, ihl);
                            int protocol = ipHeader.getProtocol();

                            // Parse the transport layer
                            if (protocol == 6) {
                                System.out.println("This is TCP packet.");
                                TCPHeader tcpHeader = new TCPHeader(tmpPacket, ihl);
                                processTCPPacket(in, out, socketManager, ipHeader, tcpHeader);
                            } else if (protocol == 17){
                                System.out.println("This is UDP packet.");
                                System.out.println("Packet Length: " + tmpPacket.length);
                                System.out.println("IP Header Length: " + ihl);
                                UDPHeader udpHeader = new UDPHeader(tmpPacket, ihl);
                                processUDPPacket(socketManager, ipHeader, udpHeader);
                            } else if (protocol == 1) {
                                System.out.println("This is ICMP packet.");
                                ICMPHeader icmpHeader = new ICMPHeader(tmpPacket, ihl);
                                System.out.println("ICMP Type: " + icmpHeader.getType());
                                System.out.println("ICMP Code: " + icmpHeader.getCode());
                                processICMPPacket(out, ipHeader, icmpHeader);
                            } else {
                                System.out.println("This is another protocol: " + protocol);
                            }
                        }
                        Thread.sleep(TIMING);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (mInterface != null) {
                            mInterface.close();
                            mInterface = null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "MyVpnRunnable");

        mThread.start();
        return START_STICKY;
    }



    private int getIhl(byte[] packet) {
        return (packet[0] & 0xf) * 4;
    }

    private void processICMPPacket(FileOutputStream out, IPHeader ipHeader, ICMPHeader icmpHeader) {

    }

    private void processTCPPacket(FileInputStream in, FileOutputStream out, SocketManager sm, IPHeader ipHeader, TCPHeader tcpHeader) {
        if (tcpHeader.getSyn()) {
            // Add the TCP Socket in the SocketManager
            try {
                SocketChannel channel = SocketChannel.open();
                protect(channel.socket());
                System.out.println("SYN- Seq from Client: " + tcpHeader.getSequenceNumber() + " " + ipHeader.getSourceIP() + ":" + tcpHeader.getSourcePort());
                tcpHeader.setAckNumber(makingSeqnum());

                if (tcpHeader.getDestPort() == 443) {
                    sm.addTLSSocket(channel, ipHeader, tcpHeader);
                } else {
                    sm.addTCPSocket(channel, ipHeader, tcpHeader);
                }
                processTCPHandshake(in, out, ipHeader, tcpHeader);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (tcpHeader.getFin()) {
            System.out.println("Get FIN packet");
            processTCPHandshake(in, out, ipHeader, tcpHeader);
            // Delete the TCP Socket in the SocketManager
            sm.delSocket(true, ipHeader.getSourceIP(), tcpHeader.getSourcePort());
        } else if (tcpHeader.getAck() && tcpHeader.getPayloadLength() == 0) {
            if (tcpHeader.getDestPort() == 443) {
                System.out.println("This is TLS. Now Start TLS Handshake.");
                processTLSHandshake(in, out, ipHeader, tcpHeader);
                //byte[] outPacket = changeDestSrc(tcpHeader, ipHeader, tcpHeader.getPayload(), ipHeader.getSourceIP(), ipHeader.getDestIP(), tcpHeader.getSourcePort(), tcpHeader.getDestPort(), tcpHeader.getSequenceNumber(), tcpHeader.getAckNumber(), "");
            }


            System.out.println("Flags from " + ipHeader.getSourceIP() + ":" + tcpHeader.getSourcePort() + ": " + tcpHeader.getFlag());
            System.out.println("ACK- TCP handshake complete");
            System.out.println("ACK) DestIP: " + ipHeader.getDestIP() + ", DestPort: " + tcpHeader.getDestPort() + ", SrcIP: " + ipHeader.getSourceIP() + ", SrcPort: " + tcpHeader.getSourcePort() + ", SEQ: " + tcpHeader.getSequenceNumber() + ", ACK: " + tcpHeader.getAckNumber() + ", Flags: " + tcpHeader.getFlag());
        } else {
            // Send the Message
            System.out.println("Send the TCP message from " + ipHeader.getSourceIP() + ":" + tcpHeader.getSourcePort() + " to " + ipHeader.getDestIP() + ":" + tcpHeader.getDestPort());
            System.out.println("TCP Message Size: " + tcpHeader.getPayloadLength());
            System.out.println("Now Finding the Package Name");
            String packageName = "";
            try {
                packageName = PackageNameFinder.getPackage(true, tcpHeader.getSourcePort(), getApplicationContext());
                System.out.println("Package Name: " + packageName);
            } catch(IOException e) {
                e.printStackTrace();
            }

            analyzePacket(packageName, tcpHeader.getPayload());
            sm.sendMessage(true, ipHeader.getSourceIP(), tcpHeader.getSourcePort(), tcpHeader.getPayload());
        }
    }

    private void analyzePacket(String packageName, byte[] payload) {
        System.out.println("In Analyze Packet) Package Name: " + packageName + ", Payload Size: " + payload.length);
        String strPayload = new String(payload);
        try{
            Message msg = Message.obtain(null, SENDPAYLOAD);
            Bundle data = new Bundle();
            data.putString("packageName", packageName);
            data.putString("payload", strPayload);
            msg.setData(data);
            mainMessenger.send(msg);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void processUDPPacket(SocketManager sm, IPHeader ipHeader, UDPHeader udpHeader) {
        // Send the Message
        System.out.println("Send the UDP message from " + ipHeader.getSourceIP() + ":" + udpHeader.getSourcePort() + " to " + ipHeader.getDestIP() + ":" + udpHeader.getDestPort());
        System.out.println("UDP Message Size: " + udpHeader.getPayloadLength());

        try {
            DatagramChannel channel = DatagramChannel.open();
            protect(channel.socket());
            if (!sm.checkSocket(false, ipHeader.getSourceIP(), udpHeader.getSourcePort()))
                sm.addUDPSocket(channel, ipHeader, udpHeader);
            sm.sendMessage(false, ipHeader.getSourceIP(), udpHeader.getSourcePort(), udpHeader.getPayload());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processTCPHandshake(FileInputStream in, FileOutputStream out, IPHeader ipHeader, TCPHeader tcpHeader) {
        String sourceIP = ipHeader.getSourceIP();
        String destIP = ipHeader.getDestIP();
        int sPort = tcpHeader.getSourcePort();
        int dPort = tcpHeader.getDestPort();
        long seqNum = tcpHeader.getSequenceNumber();
        long ackNum = tcpHeader.getAckNumber();

        byte[] outPacket = null;

        if (tcpHeader.getSyn()) {
            System.out.println("SYN packet found to " + destIP + ":" + dPort);
            System.out.println("SYN) DestIP: " + destIP + ", DestPort: " + dPort + ", SrcIP: " + sourceIP + ", SrcPort: " + sPort + ", SEQ: " + tcpHeader.getSequenceNumber() + ", ACK: " + tcpHeader.getAckNumber() + ", Flags: " + tcpHeader.getFlag());
            outPacket = changeDestSrc(tcpHeader, ipHeader, null, sourceIP, destIP, sPort, dPort, seqNum, ackNum, "syn");
        } else if (tcpHeader.getFin()) {
            System.out.println("FIN packet found to " + destIP + ":" + dPort);
            System.out.println("FIN) DestIP: " + destIP + ", DestPort: " + dPort + ", SrcIP: " + sourceIP + ", SrcPort: " + sPort + ", SEQ: " + tcpHeader.getSequenceNumber() + ", ACK: " + tcpHeader.getAckNumber() + ", Flags: " + tcpHeader.getFlag());
            outPacket = changeDestSrc(tcpHeader, ipHeader, null, sourceIP, destIP, sPort, dPort, seqNum, ackNum, "fin");
        }

        try {
            out.write(outPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processTLSHandshake(FileInputStream in, FileOutputStream out, IPHeader ipHeader, TCPHeader tcpHeader) {

    }

    private void processFINHandshake(FileInputStream in, FileOutputStream out, IPHeader ipHeader, TCPHeader tcpHeader) {
        String sourceIP = ipHeader.getSourceIP();
        String destIP = ipHeader.getDestIP();
        int sPort = tcpHeader.getSourcePort();
        int dPort = tcpHeader.getDestPort();
        int headerLength = tcpHeader.getHeaderLength();
        long seqNum = tcpHeader.getSequenceNumber();
        long ackNum = tcpHeader.getAckNumber();

        System.out.println("FIN packet found to " + destIP + ":" + dPort);

        byte[] outPacket = changeDestSrc(tcpHeader, ipHeader, null, sourceIP, destIP, sPort, dPort, seqNum ,ackNum, "fin");
        try {
            out.write(outPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long makingSeqnum(){
        return (long)(Math.random() * Integer.MAX_VALUE) + 1;
    }
    private int makingChecksum(byte[] header){               //make checksum.
        int length = header.length;
        if(length ==0){
            return 1;
        }
        int answer=0;
        for(int i=0;i<length ;i+=2){
            int tmp = header[i];
            if(i+1>=length) {               //odd
                answer = answer+(int) ((tmp&0xff)<<8);
            }
            else {
                int tmp2 = header[i+1];
                answer = answer+(int)(((tmp&0xff) << 8)|(tmp2&0xff));
            }
        }
        answer=((answer & 0xffff)+ (answer >> 16));
        return ~answer;
    }

    public byte[] changeDestSrc(TCPHeader tHeader, IPHeader ipHeader, byte[] payload , String sourceIP, String destIP, int sPort, int dPort, long seqNum, long ackNum, String state){
        ipHeader.setDestIP(sourceIP);
        ipHeader.setSourceIP(destIP);
        //change IPs.
        tHeader.setDestPort(sPort);
        tHeader.setSourcePort(dPort);

        tHeader.setAckNumber(seqNum+1);
        tHeader.setSequenceNumber(ackNum);
        switch(state) {
            case "syn":
                System.out.println("SYN/ACK- Seq from Server: " + tHeader.getSequenceNumber() + " " + ipHeader.getDestIP() + ":" + tHeader.getDestPort()) ;
                System.out.println("SYN/ACK) DestIP: " + destIP + ", DestPort: " + dPort + ", SrcIP: " + sourceIP + ", SrcPort: " + sPort + ", SEQ: " + tHeader.getSequenceNumber() + ", ACK: " + tHeader.getAckNumber() + ", Flags: " + tHeader.getFlag());
                System.out.println("SYN/ACK- Ack from Server: " + tHeader.getAckNumber() + " " + ipHeader.getDestIP() + ":" + tHeader.getDestPort());
                break;
            case "fin":
                System.out.println("ACK for FIN/ACK");
        }
        int offset = tHeader.getHeaderLength();
        byte[] ipH  = ipHeader.getHeader();
        byte[] tHeaderReader = tHeader.getHeader();

        ipH[10]=(byte)0x00;
        ipH[11]=(byte)0x00;                   // make checksum to 0.
        tHeaderReader[16]=(byte)0x00;
        tHeaderReader[17]=(byte)0x00;                                                    //make checksums to 0.
        tHeaderReader[12] = (byte)((tHeaderReader[12])&0xf1);                               //reserved
        if(state.compareTo("syn")==0)
            tHeaderReader[13] = (byte) 0x12;                                                 //make to syn ack
        else if(state.compareTo("fin")==0)
            tHeaderReader[13] = (byte) 0x10;
        else if(tHeader.getDestPort() == 443) // Temporal RST
            tHeaderReader[13] = (byte) 0x4;

        int payload_l = 0;
        if(payload != null){
            payload_l = payload.length;
        }
        byte[] pseudoTCP = new byte[tHeaderReader.length + 12+ payload_l];                         //Pseudo + TCP header.
        System.arraycopy(tHeaderReader,0 ,pseudoTCP,12,tHeaderReader.length);
        System.arraycopy(ipH ,12,pseudoTCP,0 ,8);

        pseudoTCP[8] = (byte)0;                                                        //reserved
        pseudoTCP[9] = (byte)6;
        pseudoTCP[10] = (byte) (((payload_l+offset)&0xff00)>>8);
        pseudoTCP[11] = (byte) ((payload_l+offset)&0x00ff);

        int ipChecksum = makingChecksum(ipH);
        int tcpChecksum =  makingChecksum(pseudoTCP);
        ipH[10] = (byte)((ipChecksum & 0xff00)>>8);
        ipH[11]  = (byte)(ipChecksum & 0x00ff);

        pseudoTCP[12+16] = (byte)((tcpChecksum&0xff00)>>8);
        pseudoTCP[12+17] = (byte)(tcpChecksum&0x00ff);
        byte[] outpacket = new byte[20+offset+payload_l];
        System.arraycopy(ipH,0,outpacket,0,20);
        System.arraycopy(pseudoTCP,12,outpacket,20,offset+payload_l);

        return outpacket;
    }
    @Override
    public void onDestroy() {
        if (mThread != null) {
            mThread.interrupt();
        }
        super.onDestroy();
        Log.d(TAG,"die");
    }

    public byte[] hexToByteArray(String hex) {
        if (hex == null || hex.length() == 0) {
            return null;
        }
        byte[] ba = new byte[hex.length() / 2];
        for (int i = 0; i < ba.length; i++) {
            ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return ba;
    }

    public String byteArrayToHex(byte[] ba) {
        if (ba == null || ba.length == 0) {
            return null;
        }

        StringBuffer sb = new StringBuffer(ba.length * 2);
        String hexNumber;
        for (int x = 0; x < ba.length; x++) {
            hexNumber = "0" + Integer.toHexString(0xff & ba[x]);

            sb.append(hexNumber.substring(hexNumber.length() - 2));
        }
        return sb.toString();
    }

    public String hexToIp(String addr){                     //hex to IP addr form.
        String ip = "";
        for(int i =0;i<addr.length();i=i+2){
            ip = ip+Integer.valueOf(addr.substring(i,i+2),16)+".";
        }
        return ip;
    }
}
