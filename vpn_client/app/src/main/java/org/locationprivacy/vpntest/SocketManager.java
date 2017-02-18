package org.locationprivacy.vpntest;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;

/**
 * Created by user on 2017-02-12.
 */

public class SocketManager implements SocketManagerAPI {

    private Selector selector;
    private Hashtable<String, Socket> ht;
    public SocketManager() {
        try {
            selector = Selector.open();
            ht = new Hashtable<String, Socket>();
        }
        catch (IOException e)
        {
            System.out.println(e.getStackTrace());
        }
    }

    @Override
    public void addSocket(boolean isTCP, IP_Header ipHdr, TCP_Header tcpHdr) {
        String key = ipHdr.getDestIP() + ":" + tcpHdr.getDestPort();
        if (isTCP) // This means the message is TCP
        {
            try {
                SocketChannel socket = SocketChannel.open();
                socket.configureBlocking(false);
                socket.register(selector, SelectionKey.OP_READ, null);
                ht.put(key, socket.socket());
            }
            catch (IOException e)
            {
                System.out.println(e.getStackTrace());
            }
        }
        else // This means the message is UDP
        {
            try {
                DatagramChannel socket = DatagramChannel.open();
                socket.configureBlocking(false);
                socket.register(selector, SelectionKey.OP_READ, null);
                ht.put(key, socket.socket());
            }
            catch (IOException e)
            {
                System.out.println(e.getStackTrace());
            }
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
