package org.socialcoding.privacyguardian.VPN;

import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by Hyunwoo Lee on 2017-02-06.
 */

public interface SocketManagerAPI {

    // Checking whether the socket is in the SocketManager
    boolean checkSocket(boolean isTCP, String clntIP, int clntPort);

    // Adding the socket in the SocketManager
    void addTCPSocket(SocketChannel channel, IPHeader ipHdr, TCPHeader tHdr);
    void updateTLSSocket(SocketChannel channel, SecurityParameters sp);
    void addUDPSocket(DatagramChannel channel, IPHeader ipHdr, UDPHeader tHdr);

    // Deleting the socket in the SocketManager
    void delSocket(boolean isTCP, String clntIP, int clntPort);

    // Sending the message to the corresponding server
    void sendMessage(boolean isTCP, String clntIP, int clntPort, byte[] payload);

    // Checking whether the message is in the queue
    boolean isMessage();

    // Getting the message from the queue
    byte[] getMessage();
}
