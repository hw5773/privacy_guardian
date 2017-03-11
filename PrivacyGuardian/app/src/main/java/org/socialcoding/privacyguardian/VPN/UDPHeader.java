package org.socialcoding.privacyguardian.VPN;

/**
 * Created by 신승수 on 2017-02-06.
 */

public class UDPHeader extends TransportHeader {
    private int UDP_BYTES = 8;

    UDPHeader(byte[] packet , int ihl){
        this.ihl = ihl;
        this.length = ((packet[ihl+4] & 0xff) << 8) | (packet[ihl+5] & 0xff);
        this.headerLength = UDP_BYTES;
        this.payloadLength = length - headerLength;

        header = new byte[headerLength];
        payload = new byte[payloadLength];

        for (int i = 0; i < headerLength; i++)
            header[i] = packet[ihl + i];

        for (int i = 0; i < payloadLength; i++)
            payload[i] = packet[ihl + headerLength + i];

        sPort = ((header[0] & 0xff) << 8) | (header[1] & 0xff);
        dPort = ((header[2] & 0xff) << 8) | (header[3] & 0xff);
    }
}
