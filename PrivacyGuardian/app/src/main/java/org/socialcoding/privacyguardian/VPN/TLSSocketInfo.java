package org.socialcoding.privacyguardian.VPN;
import java.nio.channels.SocketChannel;

/**
 * Created by Hyunwoo Lee on 2017-02-18.
 */

import java.nio.channels.SocketChannel;

public class TLSSocketInfo extends TCPSocketInfo {

    private SecurityParameters sp;

    public TLSSocketInfo(SocketChannel socket, String clntIPAddr, int clntPort, long seqNum, long ackNum) {
        super(socket, clntIPAddr, clntPort, seqNum, ackNum);
    }

    public void setSp(SecurityParameters sp)
    {
        this.sp = sp;
    }

    public SecurityParameters getSp()
    {
        return sp;
    }
}