package org.socialcoding.privacyguardian.VPN;

/**
 * Created by 신승수 on 2017-02-06.
 */

public abstract class TransportHeader {

    protected byte[] header;
    protected byte[] payload;
    protected int ihl; // The IP header length
    protected int length; // The length of transport layer packet, involving the payload
    protected int headerLength;
    protected int payloadLength;
    protected int sPort;
    protected int dPort;

    // Return the length of the transport layer packet, involving the length of the payload.
    int getLength() { return length; }
    int getHeaderLength() { return headerLength; }
    int getPayloadLength() { return payloadLength; }

    byte[] getHeader(){
        return header;
    } // Return the header
    byte[] getPayload() { return payload; } // Return the payload

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
}
