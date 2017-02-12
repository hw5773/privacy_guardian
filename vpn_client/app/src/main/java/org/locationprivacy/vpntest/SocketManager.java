package org.locationprivacy.vpntest;

/**
 * Created by HWY on 2017-02-06.
 */

public interface SocketManager {
    // Adding the socket in the SocketManager
    void addSocket(boolean isTCP, IP_Header ipHdr, TCP_Header tcpHdr);

    // Forwarding the message to the SocketManager
    void message(String destIP, int destPort, String payload);

    // Deleting the socket in the SocketManager
    void delSocket(String destIP, int destPort);

    // Sending the message to the corresponding server
    void sendMessage(String destIP, int destPost, String payload);

    // Checking whether the message is in the queue
    boolean isMessage();

    // Getting the message from the queue
    byte[] getMessage();
}
