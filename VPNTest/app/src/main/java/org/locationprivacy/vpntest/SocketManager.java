package org.locationprivacy.vpntest;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Queue;

/**
 * Created by user on 2017-02-12.
 */

public class SocketManager implements SocketManagerAPI {

    private Selector selector;
    private Hashtable<String, SocketChannel> tcpHt;
    private Hashtable<String, DatagramChannel> udpHt;
    private Queue<byte[]> msgQueue;

    public SocketManager() {
        try {
            selector = Selector.open();
            tcpHt = new Hashtable<String, SocketChannel>();
            udpHt = new Hashtable<String, DatagramChannel>();
            msgQueue = new Queue<byte[]>() {
                @Override
                public boolean add(byte[] bytes) {
                    return false;
                }

                @Override
                public boolean offer(byte[] bytes) {
                    return false;
                }

                @Override
                public byte[] remove() {
                    return new byte[0];
                }

                @Override
                public byte[] poll() {
                    return new byte[0];
                }

                @Override
                public byte[] element() {
                    return new byte[0];
                }

                @Override
                public byte[] peek() {
                    return new byte[0];
                }

                @Override
                public boolean addAll(Collection<? extends byte[]> collection) {
                    return false;
                }

                @Override
                public void clear() {

                }

                @Override
                public boolean contains(Object o) {
                    return false;
                }

                @Override
                public boolean containsAll(Collection<?> collection) {
                    return false;
                }

                @Override
                public boolean isEmpty() {
                    return false;
                }

                @NonNull
                @Override
                public Iterator<byte[]> iterator() {
                    return null;
                }

                @Override
                public boolean remove(Object o) {
                    return false;
                }

                @Override
                public boolean removeAll(Collection<?> collection) {
                    return false;
                }

                @Override
                public boolean retainAll(Collection<?> collection) {
                    return false;
                }

                @Override
                public int size() {
                    return 0;
                }

                @NonNull
                @Override
                public Object[] toArray() {
                    return new Object[0];
                }

                @NonNull
                @Override
                public <T> T[] toArray(T[] ts) {
                    return null;
                }
            };
        }
        catch (IOException e)
        {
            System.out.println(e.getStackTrace());
        }
    }

    @Override
    public void addSocket(boolean isTCP, IP_Header ipHdr, TransmissionHeader tcpHdr) {
        String key = ipHdr.getDestIP() + ":" + tcpHdr.getDestPort();
        if (isTCP) // This means the message is TCP
        {
            try {
                SocketChannel socket = SocketChannel.open();
                socket.configureBlocking(false);
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

    }

    @Override
    public void sendMessage(boolean isTCP, String destIP, int destPort, String payload) {

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
