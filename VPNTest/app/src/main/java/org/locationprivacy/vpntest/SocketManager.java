package org.locationprivacy.vpntest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by user Hyunwoo Lee on 2017-02-12.
 */

public class SocketManager implements SocketManagerAPI {

    private Selector selector;
    private Hashtable<String, TCPSocketInfo> tcpHt;
    private Hashtable<String, UDPSocketInfo> udpHt;
    private LinkedBlockingQueue<byte[]> msgQueue;
    private Thread mThread;

    public SocketManager() {
        try {
            selector = Selector.open();
            tcpHt = new Hashtable<String, TCPSocketInfo>();
            udpHt = new Hashtable<String, UDPSocketInfo>();
            msgQueue = new LinkedBlockingQueue<byte[]>();
            /*
            mThread = new Thread(new Runnable() {

				@Override
				public void run() {
					System.out.println("Poller starts.");
					while(true) {
			    		try {
			    			int readyChannels = selector.select();

			    			if (readyChannels == 0) continue;
			    			System.out.println("Get Some messages.");
			    			Thread.sleep(100);

			    		} catch (IOException e) {
			    			System.out.println(e.getStackTrace());
			    		} catch (InterruptedException e) {
			    			System.out.println(e.getStackTrace());
			    		}

			    	}
				}
            }, "Poller");
            */
        }
        catch (Exception e)
        {
            System.out.println(e.getStackTrace());
        }
        //mThread.start();
    }

    @Override
    public void addSocket(boolean isTCP, IP_Header ipHdr, TransmissionHeader tHdr) {

        if (isTCP) // This means the message is TCP
        {
            try {
                TCP_Header tcpHdr = (TCP_Header) tHdr;
                String key = makeKey(ipHdr.getSourceIP(), tcpHdr.getSourcePort());
                System.out.println("TCP is selected");
                System.out.println("key: " + key);
                SocketChannel socket = SocketChannel.open();
                socket.configureBlocking(false);
                socket.connect(new InetSocketAddress(ipHdr.getSourceIP(), tcpHdr.getSourcePort()));
                while (!socket.finishConnect());

                // Log message
                System.out.println("Complete to make the socket with " + key);
                System.out.println("Socket in addSocket: " + socket);

                // socket.register(selector, SelectionKey.OP_READ, null);
                selector.wakeup();
                socket.register(selector, SelectionKey.OP_READ, null);
                System.out.println("Socket is registered in the Selector");
                tcpHt.put(key, new TCPSocketInfo(socket, ipHdr.getDestIP(), tcpHdr.getDestPort(), tcpHdr.getSequenceNumber(), tcpHdr.getAckNumber()));
                System.out.println("Socket is inputted into the TCP hash table");
            }
            catch (IOException e)
            {
                System.out.println(e.getStackTrace());
            }
        }
        else // This means the message is UDP
        {
            try {
                UDP_Header udpHdr = (UDP_Header) tHdr;
                String key = makeKey(ipHdr.getDestIP(), udpHdr.getDestPort());
                System.out.println("key: " + key);
                DatagramChannel socket = DatagramChannel.open();
                socket.configureBlocking(false);
                socket.connect(new InetSocketAddress(ipHdr.getSourceIP(), udpHdr.getSourcePort()));
                socket.register(selector, SelectionKey.OP_READ, null);
                udpHt.put(key, new UDPSocketInfo(socket, ipHdr.getDestIP(), udpHdr.getDestPort()));
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
                tcpHt.get(key).getSocket().close();
                System.out.println("Now Socket is deleted.");
            } catch (IOException e) {
                System.out.println(e.getStackTrace());
            }
            tcpHt.remove(key);
        }
        else
        {
            try {
                udpHt.get(key).getSocket().close();
            } catch (IOException e) {
                System.out.println(e.getStackTrace());
            }
            udpHt.remove(key);
        }
    }

    @Override
    public void sendMessage(boolean isTCP, String destIP, int destPort, String payload) {
        String key = makeKey(destIP, destPort);
        System.out.println("key in sendMessage: " + key);
        ByteBuffer msg = ByteBuffer.wrap(payload.getBytes());
        System.out.println("msg: " + payload);

        if (isTCP)
        {
            try
            {
                System.out.println("Socket in sendMessage: " + tcpHt.get(key).getSocket());
                tcpHt.get(key).getSocket().write(msg);
                System.out.println("Write the message " + payload + " at " + tcpHt.get(key));
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
                udpHt.get(key).getSocket().write(msg);
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
    private void addMessage(byte[] msg) {
        try {
            msgQueue.put(msg);
        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace());
        }
    }
}
