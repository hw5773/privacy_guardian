package org.socialcoding.privacyguardian.VPN;

/**
 * Created by 신승수 on 2017-02-06.
 */

public class TransmissionHeader {
    protected byte[] header;
    protected int offset;
    protected int sPort;
    protected int dPort;
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
    int getOffset(){
        return offset;
    }
}
