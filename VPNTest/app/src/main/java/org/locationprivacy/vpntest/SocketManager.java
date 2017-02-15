package org.locationprivacy.vpntest;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by user on 2017-02-12.
 */

public class SocketManager implements SocketManagerAPI {

    private Selector selector;
    private Hashtable<String, SocketChannel> tcpHt;
    private Hashtable<String, DatagramChannel> udpHt;
    private LinkedBlockingQueue<byte[]> msgQueue;

    public SocketManager() {
        try {
            selector = Selector.open();
            tcpHt = new Hashtable<String, SocketChannel>();
            udpHt = new Hashtable<String, DatagramChannel>();
            msgQueue = new LinkedBlockingQueue<byte[]>();
        }
        catch (IOException e)
        {
            System.out.println(e.getStackTrace());
        }
    }

    @Override
    public void addSocket(boolean isTCP, IP_Header ipHdr, TransmissionHeader tHdr) {
        String key = makeKey(ipHdr.getDestIP(), tHdr.getDestPort());

        if (isTCP) // This means the message is TCP
        {
            try {
                SocketChannel socket = SocketChannel.open();
                socket.configureBlocking(false);
                socket.connect(new InetSocketAddress(ipHdr.getDestIP(), tHdr.getDestPort()));
                socket.register(selector, SelectionKey.OP_READ, null);
                tcpHt.put(key, socket);
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
                socket.connect(new InetSocketAddress(ipHdr.getDestIP(), tHdr.getDestPort()));
                socket.register(selector, SelectionKey.OP_READ, null);
                udpHt.put(key, socket);
            }
            catch (IOException e)
            {
                System.out.println(e.getStackTrace());
            }
        }
    }

    @Override
    public void delSocket(boolean isTCP, String destIP, int destPort) {
        String key = makeKey(destIP, destPort);

        if (isTCP)
        {
            try {
                tcpHt.get(key).close();
            } catch (IOException e) {
                System.out.println(e.getStackTrace());
            }
            tcpHt.remove(key);
        }
        else
        {
            try {
                udpHt.get(key).close();
            } catch (IOException e) {
                System.out.println(e.getStackTrace());
            }
            udpHt.remove(key);
        }
    }

    @Override
    public void sendMessage(boolean isTCP, String destIP, int destPort, String payload) {
        String key = makeKey(destIP, destPort);
        ByteBuffer msg = ByteBuffer.wrap(payload.getBytes());

        if (isTCP)
        {
            try
            {
                tcpHt.get(key).write(msg);
            }
            catch (IOException e)
            {
                System.out.println(e.getStackTrace());
            }
        }
        else
        {
            try
            {
                udpHt.get(key).write(msg);
            }
            catch (IOException e)
            {
                System.out.println(e.getStackTrace());
            }
        }
    }

    @Override
    public boolean isMessage() {
        return !(msgQueue.isEmpty());
    }

    @Override
    public byte[] getMessage() {
        return msgQueue.poll();
    }

    private String makeKey(String destIP, int destPort)
    {
        return destIP + ":" + destPort;
    }
}
