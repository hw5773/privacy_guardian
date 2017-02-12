package org.locationprivacy.vpntest;

/**
 * Created by user on 2017-02-12.
 */

public class SocketManager implements SocketManagerAPI {

    public SocketManager() {
    }

    @Override
    public void addSocket(boolean isTCP, IP_Header ipHdr, TCP_Header tcpHdr) {
        if (isTCP == true) // This means the message is TCP
        {

        }
        else if(isTCP == false) // This means the message is UDP
        {

        }
    }

    @Override
    public void delSocket(String destIP, int destPort) {

    }

    @Override
    public void sendMessage(String destIP, int destPort, String payload) {

    }

    @Override
    public boolean isMessage() {
        return false;
    }

    @Override
    public byte[] getMessage() {
        return new byte[0];
    }
}
