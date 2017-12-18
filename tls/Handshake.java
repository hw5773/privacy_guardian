package org.socialcoding.privacyguardian.VPN;

import java.nio.ByteBuffer;

/**
 * Created by HWY on 2017-07-11.
 */

public class Handshake extends org.socialcoding.privacyguardian.VPN.Record {
    private HandshakeType msgType;
    int length;
}

class SessionID {
    private byte[] sessionID;

    public SessionID() {
        for (int i=0; i<32; i++) {
            sessionID[i] = (byte) (Math.random() * 256);
        }
    }

    public void setSessionID() {
        for (int i=0; i<32; i++) {
            sessionID[i] = (byte) (Math.random() * 256);
        }
    }

    public byte[] getSessionID() { return sessionID; }
}

class CipherSuite {
    private byte[] ciphersuite;

    public CipherSuite(short cs) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(cs);
        ciphersuite = buffer.array();
    }

    public void setCiphersuite(short cs) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(cs);
        ciphersuite = buffer.array();
    }

    public byte[] getCiphersuite() { return ciphersuite; }
}

enum HandshakeType {
    hello_request(0), client_hello(1), server_hello(2), certificate(11),
    server_key_exchange(12), certificate_request(13), server_hello_done(14), certificate_verify(15),
    client_key_exchange(16), finished(20);

    private final int number;
    HandshakeType(int number) {
        this.number = number;
    }

    int getMagicNumber() { return number; }
};

enum CompressionMethod {
    no(0), yes(255);
    private final int number;
    CompressionMethod(int number) {
        this.number = number;
    }

    int getMagicNumber() { return number; }
};