package org.socialcoding.privacyguardian.VPN;

/**
 * Created by Hyunwoo Lee on 2017-02-06.
 */

public interface SocketManagerAPI {
    // Adding the socket in the SocketManager
    void addSocket(boolean isTCP, IP_Header ipHdr, TransmissionHeader tHdr);

    // Deleting the socket in the SocketManager
    void delSocket(boolean isTCP, String clntIP, int clntPort);

    // Sending the message to the corresponding server
    void sendMessage(boolean isTCP, String clntIP, int clntPort, String payload);

    // Checking whether the message is in the queue
    boolean isMessage();

    // Getting the message from the queue
    byte[] getMessage();
}
