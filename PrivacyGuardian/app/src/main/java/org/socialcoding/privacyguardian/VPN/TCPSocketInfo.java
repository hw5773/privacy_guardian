package org.socialcoding.privacyguardian.VPN;
import java.nio.channels.SocketChannel;

/**
 * Created by Hyunwoo Lee on 2017-02-18.
 */

import java.nio.channels.SocketChannel;

public class TCPSocketInfo {

    private SocketChannel socket;
    private String clntIPAddr;
    private int clntPort;
    private long seqNum;
    private long ackNum;
    private int identification;

    public TCPSocketInfo(SocketChannel socket, String clntIPAddr, int clntPort, long seqNum, long ackNum) {
        this.socket = socket;
        this.clntIPAddr = clntIPAddr;
        this.clntPort = clntPort;
        this.seqNum = seqNum;
        this.ackNum = ackNum;
        this.identification = (int) (Math.random() * Integer.MAX_VALUE);
    }

    public SocketChannel getSocket() {
        return socket;
    }

    public String getClntIPAddr() {
        return clntIPAddr;
    }

    public int getClntPort() {
        return clntPort;
    }

    public void setSeqNum(int dataLength) {
        seqNum = seqNum + dataLength;
    }

    public long getSeqNum() {
        return seqNum;
    }

    public void setAckNum(int dataLength) {
        ackNum = ackNum + dataLength;
    }

    public long getAckNum() {
        return ackNum;
    }

    public int getIdentification() {
        identification = identification + 1;
        return identification;
    }
}