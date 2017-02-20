package org.socialcoding.privacyguardian.VPN;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.StringTokenizer;


/**
 * Created by user on 2016-08-14.
 */
public class Vpn extends VpnService {
    private static final String TAG = "VpnServiceTest";
    private Thread mThread;
    private ParcelFileDescriptor mInterface;
    Builder builder = new Builder();
    private Context mContext = null;
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        mContext = getApplicationContext();
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mInterface = builder.setSession("MyVPNService")
                            .addAddress("192.168.0.1", 24)
                            .addDnsServer("8.8.8.8")
                            .addRoute("0.0.0.0", 0).establish();
                    FileInputStream in = new FileInputStream(
                            mInterface.getFileDescriptor());
                    FileOutputStream out = new FileOutputStream(
                            mInterface.getFileDescriptor());

                    SocketChannel socketChannel = SocketChannel.open();
                    protect(socketChannel.socket());                                       //protection
                    SocketManager socketManager = new SocketManager();

                    int length = 0;                                                        //length of packet.
                    ByteBuffer packet = ByteBuffer.allocate(2048);
                    //DBHelper socketDB = new DBHelper(getApplicationContext(),"SOCKETDB.db",null,1);
                    SocketDB socketDB = new SocketDB();
                    while (true) {
                        while(socketManager.isMessage()) {                  //is there any messages to send to
                            out.write(socketManager.getMessage());
                        }
                        length = in.read(packet.array());
                        if (length > 0) {
                            boolean handshake = false;
                            boolean TCPchecker;
                            packet.limit(length);
                            byte[] temp_packet = packet.array();
                            packet.clear();
                            BufferedReader networkReader;

                            int ihl = get_Ihl(temp_packet);                                             // length of ip header.
                            IP_Header IP_h = new IP_Header(temp_packet, ihl);
                            Log.d(TAG, "" + ihl);

                            String sourceIP = IP_h.getSourceIP();
                            String destIP = IP_h.getDestIP();
                            TCPchecker = IP_h.getProtocol();
                            int sPort, dPort, offset;

                            if (TCPchecker) {                                 //TCP header
                                TCP_Header T_header = new TCP_Header(temp_packet, ihl);

                                sPort = T_header.getSourcePort();
                                dPort = T_header.getDestPort();
                                offset = T_header.getOffset();
                                long seqNum = T_header.getSequenceNumber();
                                long ackNum = T_header.getAckNumber();
                                int syn = T_header.getSyn();
                                int ack = T_header.getAck();
                                int fin = T_header.getFin();
                                if (syn != 1)
                                    handshake = true;
                                if(fin == 1 ){                      //finish the socket channel.
                                    byte[] outpacket = changeDestSrc(T_header, IP_h, null, sourceIP, destIP, sPort, dPort, seqNum ,ackNum, "fin");
                                    out.write(outpacket,0,length);
                                    socketManager.delSocket(TCPchecker,destIP,dPort);
                                }
                                if (syn == 1 && ack == 0) {
                                    byte[] outpacket = changeDestSrc(T_header, IP_h, null, sourceIP, destIP, sPort, dPort, seqNum, ackNum, "syn");
                                    out.write(outpacket, 0, length);
                                    T_header.setHeader(outpacket);
                                    socketManager.addSocket(TCPchecker, IP_h, T_header);
                                }
                            } else {              //UDP
                                handshake = true;
                                UDP_Header T_header = new UDP_Header(temp_packet, ihl);
                                offset = T_header.getOffset();
                                dPort = T_header.getDestPort();
                                sPort = T_header.getSourcePort();
                                socketManager.addSocket(TCPchecker,IP_h,T_header);
                            }
                            if (handshake) {                   //transmission start.
                                byte[] tmpdata = new byte[length - ihl - offset];
                                System.arraycopy(temp_packet, (ihl + offset), tmpdata, 0, (length - ihl - offset));
                                String SendingData = new String(tmpdata);              //actual payload.
                                if (SendingData.length() == 0) {
                                    continue;
                                } else {
                                    socketManager.sendMessage(TCPchecker,destIP,dPort,SendingData);
                                    PackageNameFinder mainOps = new PackageNameFinder(dPort, destIP, SendingData, mContext);
                                    String packagename = mainOps.getPackage();
                                    Log.d(TAG, "dest port and IP : " + String.valueOf(dPort) + " : " + destIP);


                                    //SocketAddress addr = new InetSocketAddress(destIP, dPort);
                                    //socketChannel.connect(addr);
                                    //networkReader = new BufferedReader(new InputStreamReader(socketChannel.socket().getInputStream()));

                                    //byte[] bytes = SendingData.getBytes();
                                    //     ByteBuffer buffer = ByteBuffer.wrap(tmpdata);
                                    //socketChannel.write(buffer);
                                    //         String rcvData;
                                    //       String payLoad = "";
                                    //while ((rcvData = networkReader.readLine()) != null) {
                                    //받은 payload.
                                    //  payLoad = payLoad + rcvData;
                                    // rcvData = rcvData.replaceAll("\r\n", "");
                                    // Log.d(TAG, "Received Data :" + rcvData);
                                    // }
                                    // byte[] Rdata = new byte[payLoad.length()];
                                    //           Rdata = payLoad.getBytes();
                                    //public byte[] changeDestSrc(T_header,IP_h, Rdata ,sourceIP, destIP, sPort,  dPort, seqNum, syn);
                                }
                            }
                        }
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (mInterface != null) {
                            mInterface.close();
                            mInterface = null;
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }, "MyVpnRunnable");

        mThread.start();
        return START_STICKY;
    }
    public long makingSeqnum(){
        return (long)(Math.random() * Integer.MAX_VALUE) + 1;
    }
    public int makingChecksum(byte[] header){               //make checksum.
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
        answer=((answer&0xffff)+ (answer>>16));
        return ~answer;
    }

    public byte[] changeDestSrc(TCP_Header T_header,IP_Header IP_h, byte[] payload ,String sourceIP,String destIP,int sPort, int dPort,long seqNum,long ackNum,String state){
        IP_h.setDestIP(sourceIP);
        IP_h.setSourceIP(destIP);
        //change IPs.
        T_header.setDestPort(sPort);
        T_header.setSourcePort(dPort);

        T_header.setAckNum(seqNum+1);
        long newSeqNum=0;
        switch(state){
            case "syn":
                newSeqNum = makingSeqnum();
                break;
            case "fin":
                newSeqNum = ackNum;
                break;
            default:
                Log.d(TAG,"???what state");
        }
        T_header.setSequenceNumber(newSeqNum);
        int offset = T_header.getOffset();
        byte[] IP_header  = IP_h.getHeader();
        byte[] T_headereader = T_header.getHeader();

        IP_header[10]=(byte)0x00;
        IP_header[11]=(byte)0x00;                   // make checksum to 0.
        T_headereader[16]=(byte)0x00;
        T_headereader[17]=(byte)0x00;                                                    //make checksums to 0.
        T_headereader[12] = (byte)((T_headereader[12])&0xf1);                               //reserved
        if(state.compareTo("syn")==0)
            T_headereader[13] = (byte) 0x12;                                                 //make to syn ack
        else if(state.compareTo("fin")==0)
            T_headereader[13] = (byte) 0x11;

        int payload_l = payload.length;
        byte[] pseudoTCP = new byte[T_headereader.length + 12+ payload_l];                         //Pseudo + TCP header.
        System.arraycopy(T_headereader,0 ,pseudoTCP,12,T_headereader.length);
        System.arraycopy(IP_header ,12,pseudoTCP,0 ,8);
        System.arraycopy(payload,0,pseudoTCP,T_headereader.length + 12,payload_l);

        pseudoTCP[8] = (byte)0;                                                        //reserved
        pseudoTCP[9] = (byte)6;
        pseudoTCP[10] = (byte) (((payload_l+offset)&0xff00)>>8);
        pseudoTCP[11] = (byte) ((payload_l+offset)&0x00ff);

        int IPchecksum = makingChecksum(IP_header);
        int TCPchecksum =  makingChecksum(pseudoTCP);
        IP_header[10] = (byte)((IPchecksum & 0xff00)>>8);
        IP_header[11]  = (byte)(IPchecksum & 0x00ff);

        pseudoTCP[12+16] = (byte)((TCPchecksum&0xff00)>>8);
        pseudoTCP[12+17] = (byte)(TCPchecksum&0x00ff);
        byte[] outpacket = new byte[20+offset+payload_l];
        System.arraycopy(IP_header,0,outpacket,0,20);
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

    public int get_Ihl(byte[] packet){
        return ((int)(packet[0] & 0x0f)) * 4;
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
