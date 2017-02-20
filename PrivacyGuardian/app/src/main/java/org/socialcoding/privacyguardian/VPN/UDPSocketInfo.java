package org.socialcoding.privacyguardian.VPN;

import java.nio.channels.DatagramChannel;

public class UDPSocketInfo {

    private DatagramChannel socket;
    private String clntIPAddr;
    private int clntPort;
    private int identification;

    public UDPSocketInfo(DatagramChannel socket, String clntIPAddr, int clntPort) {
        this.socket = socket;
        this.clntIPAddr = clntIPAddr;
        this.clntPort = clntPort;
        this.identification = (int) (Math.random() * Integer.MAX_VALUE);
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

    public int getIdentification() {
        identification = (identification + 1) % Integer.MAX_VALUE;
        return identification;
    }
}
