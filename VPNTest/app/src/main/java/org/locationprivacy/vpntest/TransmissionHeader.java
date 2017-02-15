package org.locationprivacy.vpntest;

/**
 * Created by 신승수 on 2017-02-06.
 */

public class TransmissionHeader {
    private byte[] header;
    protected int offset;
    private int sPort;
    private int dPort;
    byte[] getHeader(){
        return header;
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
