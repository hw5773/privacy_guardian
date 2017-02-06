package org.locationprivacy.vpntest;

import android.util.Log;

/**
 * Created by 신승수 on 2016-10-25.
 */
public class TCP_Header {
    private byte[] header;
    private int ihl;
    private int offset;
    private int sPort;
    private int dPort;
    private long SequenceNumber;
    private long AckNumber;

    TCP_Header(byte[] packet ,int ipheaderlength){
        ihl = ipheaderlength;
        offset = ((int)((packet[ihl+12] & 0xf0) >> 4)) * 4;

        Log.d("asdf",packet.length+" \n offset is "+offset);
        if(offset == 0){
            Log.d("asdf","??");
        }
        header = new byte[offset];
        for(int i = 0;i<offset;i++)
            header[i] = packet[ihl+i];                  //make header

        sPort = ((header[0] & 0xff) << 8) | (header[1] & 0xff);
        dPort = ((header[2] & 0xff) << 8) | (header[3] & 0xff);
        SequenceNumber = (((header[4] &0xff) << 24) | ((header[5] &0xff) << 16) | ((header[6] &0xff) << 8) | (header[7] &0xff)) & 0xffffffff;
        AckNumber = (((header[8] &0xff) << 24) | ((header[9] &0xff) << 16) | ((header[10] &0xff) << 8) | (header[11] &0xff)) & 0xffffffff;
    }
    byte[] getHeader(){
        return header;
    }
    void setHeader(byte[] h){
        System.arraycopy(h,0,header,0,h.length);
    }
    int getSourcePort(){
        return sPort;
    }
    void setSourcePort(int port){
        sPort = port;
        header[0] = (byte)((port)>>8);
        header[1] = (byte)(port);
    }
    int getDestPort(){
        return dPort;
    }
    void setDestPort(int port){
        dPort = port;
        header[2] = (byte)((port)>>8);
        header[3] = (byte)(port);
    }
    long getSequenceNumber(){
        return SequenceNumber;
    }
    void setSequenceNumber(long sequenceNumber){
        SequenceNumber = sequenceNumber;
        header[4]= (byte)(sequenceNumber>>24);
        header[5]= (byte)(sequenceNumber>>16);
        header[6]= (byte)(sequenceNumber>>8);
        header[7]= (byte)(sequenceNumber);
    }
    int getOffset(){
        return offset;
    }
    void setOffset(int inoffset){
        offset = inoffset;
        header[12] = (byte)((inoffset/4)<<4);
    }
    int getSyn(){
        return (header[13] & 0x2) >> 1;
    }
    int getAck(){
        return (header[13] & 0x10) >> 4;
    }
    long getAckNumber(){return  AckNumber;}
    void setAckNum(long ackNum){
        AckNumber = ackNum;
        header[8]= (byte)(ackNum>>24);
        header[9]= (byte)(ackNum>>16);
        header[10]= (byte)(ackNum>>8);
        header[11]= (byte)(ackNum);
    }
}
