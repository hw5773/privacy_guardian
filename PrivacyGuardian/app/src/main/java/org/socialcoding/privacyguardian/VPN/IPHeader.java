package org.socialcoding.privacyguardian.VPN;

import java.util.StringTokenizer;

/**
 * Created by 신승수 on 2016-10-25.
 */
public class IPHeader {
    private byte[] header;
    private int ihl;
    private int protocol;
    private String SourceIP;
    private String DestIP;
    IPHeader(byte[] packet, int length) {
        header = new byte[length];
        ihl = length;
        for (int i = 0; i < length; i++)
            header[i] = packet[i];              //get header.
        protocol = (int)(header[9]&0xff);
        SourceIP=(int) (header[12] & 0xff) + "." + (int) (header[13] & 0xff) + "." + (int) (header[14] & 0xff) + "." + (int) (header[15] & 0xff);
        DestIP = (int)(header[16] & 0xff) + "." + (int)(header[17] & 0xff) + "." +  (int)(header[18] & 0xff) + "." + (int)(header[19] & 0xff);
    }
    boolean getProtocol(){
        if(protocol==6)
            return true;
        else if(protocol==17)
            return false;
        else return false;
    }
    void setHeader(byte[] h){
        System.arraycopy(h,0,header,0,h.length);
    }
    byte[] getHeader(){
        return header;
    }
    void setSourceIP(String IP){
        SourceIP = IP;
        StringTokenizer IPtoken = new StringTokenizer(IP,".");
        header[12] = Integer.valueOf(IPtoken.nextToken()).byteValue();
        header[13] = Integer.valueOf(IPtoken.nextToken()).byteValue();
        header[14] = Integer.valueOf(IPtoken.nextToken()).byteValue();
        header[15] = Integer.valueOf(IPtoken.nextToken()).byteValue();
    }
    String getSourceIP() {
       return SourceIP;
    }
    void setDestIP(String IP){
        DestIP = IP;
        StringTokenizer IPtoken = new StringTokenizer(IP,".");
        header[16] = (Integer.valueOf(IPtoken.nextToken())).byteValue();
        header[17] =(Integer.valueOf(IPtoken.nextToken())).byteValue();
        header[18] =(Integer.valueOf(IPtoken.nextToken())).byteValue();
        header[19] =(Integer.valueOf(IPtoken.nextToken())).byteValue();
    }
    String getDestIP(){
        return DestIP;
    }
}
