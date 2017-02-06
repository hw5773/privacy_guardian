package org.locationprivacy.vpntest;

/**
 * Created by HWY on 2017-02-06.
 */

public interface SocketManager {
    // Adding the socket in the SocketManager
    public void addSocket(boolean isTCP, IP_Header ipHdr, TCP_Header tcpHdr);

    // Forwarding the message to the SocketManager
    public void message(String destIP, int destPort, String payload);

    // Deleting the socket in the SocketManager
    public void delSocket(String destIP, int destPort);
}
