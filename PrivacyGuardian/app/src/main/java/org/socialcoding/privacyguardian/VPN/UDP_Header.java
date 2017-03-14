package org.socialcoding.privacyguardian.VPN;

import android.util.Log;

/**
 * Created by 신승수 on 2017-02-06.
 */

public class UDP_Header extends TransmissionHeader {
    private int ihl;

    UDP_Header(byte[] packet ,int ipheaderlength){
        ihl = ipheaderlength;
        offset = (packet[ihl+5] & 0xff) | ((packet[ihl+4] & 0xff) << 8);
        if(offset == 0)
            Log.d("VpnService","??");

        header = new byte[offset];
        for(int i = 0;i<offset;i++)
            header[i] = packet[ihl+i];                  //make header
        sPort = ((header[0] & 0xff) << 8) | (header[1] & 0xff);
        dPort = ((header[2] & 0xff) << 8) | (header[3] & 0xff);
    }
    void setOffset(int inlength){
        offset = inlength;

        header[4] = (byte)(offset/256);
        header[5] = (byte)(offset%256);
    }
}