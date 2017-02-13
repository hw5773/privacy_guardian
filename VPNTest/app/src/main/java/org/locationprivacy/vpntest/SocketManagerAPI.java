package org.locationprivacy.vpntest;

/**
 * Created by HWY on 2017-02-06.
 */

public interface SocketManagerAPI {
    // Adding the socket in the SocketManager
    void addSocket(boolean isTCP, IP_Header ipHdr, TransmissionHeader tHdr);

    // Deleting the socket in the SocketManager
    void delSocket(boolean isTCP, String destIP, int destPort);

    // Sending the message to the corresponding server
    void sendMessage(boolean isTCP, String destIP, int destPort, String payload);

    // Checking whether the message is in the queue
    boolean isMessage();

    // Getting the message from the queue
    byte[] getMessage();
}
