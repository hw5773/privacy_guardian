package org.locationprivacy.vpntest;

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

                    int length = 0;                                                        //length of packet.
                    ByteBuffer packet = ByteBuffer.allocate(2048);
                    //DBHelper socketDB = new DBHelper(getApplicationContext(),"SOCKETDB.db",null,1);
                    SocketDB socketDB = new SocketDB();
                    while (true) {
                        length = in.read(packet.array());
                        if (length > 0) {
                            boolean handshake=false;
                            packet.limit(length);
                            byte[] temp_packet = packet.array();
                            packet.clear();
                            BufferedReader networkReader;

                            int ihl = get_Ihl(temp_packet);                                             // length of ip header.
                            IP_Header IP_h = new IP_Header(temp_packet ,ihl);
                            Log.d(TAG,""+ihl);
                            TCP_Header TCP_h = new TCP_Header(temp_packet ,ihl);

                            String sourceIP = IP_h.getSourceIP();
                            String destIP = IP_h.getDestIP();

                            int sPort = TCP_h.getSourcePort();
                            int dPort = TCP_h.getDestPort();
                            int offset = TCP_h.getOffset();
                            long seqNum = TCP_h.getSequenceNumber();
                            long ackNum = TCP_h.getAckNumber();
                            int syn = TCP_h.getSyn();
                            int ack = TCP_h.getAck();
                            Log.d(TAG, "source "+sourceIP + "    dest: "+destIP);
                            Log.d(TAG, "syn :" + syn+ "    ack: "+ack );
                            handshake = socketDB.find(sourceIP,sPort);
                            //handshake = socketDB.find(destIP,dPort);
                            if (!handshake&&(sourceIP.contains("147.46.215.152") || destIP.contains("147.46.215.152"))) {
                                if (syn == 1 && ack == 0) {
                                    byte[] outpacket = changeDestSrc(TCP_h,IP_h,null,sourceIP,destIP,sPort,dPort,seqNum,syn);

                                    //Log.d (TAG,offset+":"+((outpacket[ihl+12]&0xf0)>>4)*4);
                                    syn = (outpacket[ihl+13]&0x0f)>>1;
                                    ack = (outpacket[ihl+13]&0xf0)>>4;
                                    Log.d(TAG,"Old seqnum: "+ seqNum+ ": "+TCP_h.getAckNumber());
                                    //Log.d(TAG, "new sequence number: " + newSeqNum+ ": " + TCP_h.getSequenceNumber());
                                    Log.d(TAG, "syn changed: " + syn);
                                    Log.d(TAG, "ack changed: " + ack);
                                    Log.d(TAG, "IP check" + (makingChecksum(outpacket)&0xffff));
                                    //Log.d(TAG, "TCP check" + TCPchecksum);// (makingChecksum(TCP_header)&0xffff));
                                    out.write(outpacket, 0, length);
                                }
                                 if(syn == 0 && ack == 1 && !handshake) {
                                    Log.d(TAG, "TCP handshake complete!");
                                }
                                if(handshake){                   //transmission start.
                                    byte[] tmpdata = new byte[length - ihl - offset];
                                    System.arraycopy(temp_packet, (ihl + offset), tmpdata, 0, (length - ihl - offset));
                                    String SendingData =  new String(tmpdata);              //actual payload.

                                    Analyzer mainOps = new Analyzer(dPort,destIP,SendingData,mContext);
                                    String  packagename = mainOps.getPID();
                                    Log.d(TAG,"dest port and IP : " + String.valueOf(dPort) + " : "+ destIP);
                                    SocketAddress addr = new InetSocketAddress(destIP,dPort);
                                    socketChannel.connect(addr);
                                    networkReader = new BufferedReader(new InputStreamReader(socketChannel.socket().getInputStream()));

                                    //byte[] bytes = SendingData.getBytes();
                                    ByteBuffer buffer=ByteBuffer.wrap(tmpdata);
                                    socketChannel.write(buffer);
                                    String rcvData;
                                    String payLoad = "";
                                    while ((rcvData = networkReader.readLine())!=null) {
                                                   //받은 payload.
                                        payLoad = payLoad+rcvData;
                                        rcvData = rcvData.replaceAll("\r\n","");
                                        Log.d(TAG, "Received Data :"+ rcvData);
                                    }
                                    byte[] Rdata = new byte[payLoad.length()];
                                    Rdata =payLoad.getBytes();
                                    //public byte[] changeDestSrc(TCP_h,IP_h, Rdata ,sourceIP, destIP, sPort,  dPort, seqNum, syn);
                                }
                            }
                            IP_h=null;
                            TCP_h = null;
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

    public byte[] changeDestSrc(TCP_Header TCP_h,IP_Header IP_h, byte[] payload ,String sourceIP,String destIP,int sPort, int dPort,long seqNum,int syn){
        IP_h.setDestIP(sourceIP);
        IP_h.setSourceIP(destIP);
        //change IPs.
        TCP_h.setDestPort(sPort);
        TCP_h.setSourcePort(dPort);

        TCP_h.setAckNum(seqNum+1);

        long newSeqNum = makingSeqnum();
        TCP_h.setSequenceNumber(newSeqNum);
        int offset = TCP_h.getOffset();
        byte[] IP_header  = IP_h.getHeader();
        byte[] TCP_header = TCP_h.getHeader();

        IP_header[10]=(byte)0x00;
        IP_header[11]=(byte)0x00;                   // make checksum to 0.
        TCP_header[16]=(byte)0x00;
        TCP_header[17]=(byte)0x00;                                                    //make checksums to 0.
        TCP_header[12] = (byte)((TCP_header[12])&0xf1);                               //reserved
        if(syn==1)
            TCP_header[13] = (byte) 0x12;                                                 //make to syn ack
        else
            TCP_header[13] = (byte) 0x10;

        int payload_l = payload.length;
        byte[] pseudoTCP = new byte[TCP_header.length + 12+ payload_l];                         //Pseudo + TCP header.
        System.arraycopy(TCP_header,0 ,pseudoTCP,12,TCP_header.length);
        System.arraycopy(IP_header ,12,pseudoTCP,0 ,8);
        System.arraycopy(payload,0,pseudoTCP,TCP_header.length + 12,payload_l);

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
