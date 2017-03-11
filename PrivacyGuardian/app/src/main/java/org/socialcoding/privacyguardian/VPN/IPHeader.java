package org.socialcoding.privacyguardian.VPN;

import java.util.StringTokenizer;

/**
 * Created by 신승수 on 2016-10-25.
 */
public class IPHeader {
    private byte[] header;
    private int ihl;
    private int protocol;
    private int totalLength;
    private int identifier;
    private String sourceIP;
    private String destIP;
    final private int IP_HEADER_LENGTH = 20;

    IPHeader() {
        this.ihl = IP_HEADER_LENGTH;
        header = new byte[ihl];
        header[0] = 0x45;
        header[1] = 0x00;
    }

    IPHeader(byte[] packet, int length) {
        header = new byte[length];
        ihl = length;
        for (int i = 0; i < length; i++)
            header[i] = packet[i];              //get header.
        protocol = (int)(header[9]&0xff);
        sourceIP =(int) (header[12] & 0xff) + "." + (int) (header[13] & 0xff) + "." + (int) (header[14] & 0xff) + "." + (int) (header[15] & 0xff);
        destIP = (int)(header[16] & 0xff) + "." + (int)(header[17] & 0xff) + "." +  (int)(header[18] & 0xff) + "." + (int)(header[19] & 0xff);
    }

    void setTCP() { protocol = 0x06; header[9] = (byte) (protocol & 0xff); }
    void setUDP() { protocol = 0x11; header[9] = (byte) (protocol & 0xff); }
    int getProtocol() {
        return protocol;
    }

    void setHeader(byte[] h){
        System.arraycopy(h,0,header,0,h.length);
    }
    byte[] getHeader(){
        return header;
    }

    void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
        header[2] = (byte) ((totalLength & 0xff00) >> 8);
        header[3] = (byte) (totalLength & 0xff);
        header[6] = 0x00;
        header[7] = 0x00;
        header[8] = 0x40;
        header[10] = 0x00;
        header[11] = 0x00;
    }
    int getTotalLength() { return totalLength; }

    void setIdentifier(int identifier) {
        this.identifier = identifier;
        header[4] = (byte) ((identifier & 0xff00) >> 8);
        header[5] = (byte) (identifier & 0xff);
    }
    int getIdentifier() { return identifier; }

    void setSourceIP(String IP){
        sourceIP = IP;
        StringTokenizer IPtoken = new StringTokenizer(IP,".");
        header[12] = Integer.valueOf(IPtoken.nextToken()).byteValue();
        header[13] = Integer.valueOf(IPtoken.nextToken()).byteValue();
        header[14] = Integer.valueOf(IPtoken.nextToken()).byteValue();
        header[15] = Integer.valueOf(IPtoken.nextToken()).byteValue();
    }
    String getSourceIP() {
       return sourceIP;
    }

    void setDestIP(String IP){
        destIP = IP;
        StringTokenizer IPtoken = new StringTokenizer(IP,".");
        header[16] = Integer.valueOf(IPtoken.nextToken()).byteValue();
        header[17] = Integer.valueOf(IPtoken.nextToken()).byteValue();
        header[18] = Integer.valueOf(IPtoken.nextToken()).byteValue();
        header[19] = Integer.valueOf(IPtoken.nextToken()).byteValue();
    }
    String getDestIP(){
        return destIP;
    }

    void setChecksum(int checksum) {
        header[10] = (byte) ((checksum & 0xff00) >> 8);
        header[11] = (byte) (checksum &0xff);
    }
}
