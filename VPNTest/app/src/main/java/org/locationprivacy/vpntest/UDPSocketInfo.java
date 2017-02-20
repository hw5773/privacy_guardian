package org.locationprivacy.vpntest;

/**
 * Created by Hyunwoo Lee on 2017-02-18.
 */

import java.nio.channels.DatagramChannel;

public class UDPSocketInfo {

    private DatagramChannel socket;
    private String clntIPAddr;
    private int clntPort;

    public UDPSocketInfo(DatagramChannel socket, String clntIPAddr, int clntPort) {
        this.socket = socket;
        this.clntIPAddr = clntIPAddr;
        this.clntPort = clntPort;
    }

    public DatagramChannel getSocket() {
        return socket;
    }

    public String getClntIPAddr() {
        return clntIPAddr;
    }

    public int getClntPort() {
        return clntPort;
    }
}
