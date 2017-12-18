package org.socialcoding.privacyguardian.VPN;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by HWY on 2017-07-11.
 */

public class Record {
    ContentType type;
    ProtocolVersion version;
    short length;

    public Record() {};
    public Record(ContentType type, ProtocolVersion version, short length) {
        this.type = type;
        this.version = version;
        this.length = length;
    }

    public void setType(ContentType type) { this.type = type; }
    public void setVersion(ProtocolVersion version) { this.version = version; }
    public void setLength(short length) { this.length = length; }

    public ContentType getType() { return type; }
    public ProtocolVersion getVersion() { return version; }
    public short getLength() { return length; }
}

class ProtocolVersion {
    private byte major;
    private byte minor;

    public ProtocolVersion() {}
    public ProtocolVersion(byte major, byte minor) {
        this.major = major;
        this.minor = minor;
    }

    public void setMajor(byte major) {
        this.major = major;
    }

    public void setMinor(byte minor) {
        this.minor = minor;
    }

    public byte getMajor() {
        return major;
    }

    public byte getMinor() {
        return minor;
    }
}

class Random {
    private int gmtUnixTime;
    private byte[] randomBytes;

    public Random() throws NoSuchAlgorithmException {
        gmtUnixTime = (int) System.currentTimeMillis() / 1000;
        randomBytes = new byte[28];
        SecureRandom csprng = SecureRandom.getInstance("SHA1PRNG");
        csprng.nextBytes(randomBytes);
    }

    public void setUnixTime() {
        this.gmtUnixTime = (int) System.currentTimeMillis() / 1000;
    }

    public int getUnixTime() { return gmtUnixTime; }
    public byte[] getRandomBytes() { return randomBytes; }
    public byte[] getRandom() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(gmtUnixTime);
        byte[] ts = buffer.array();

        ByteArrayOutputStream random = new ByteArrayOutputStream();
        random.write(ts);
        random.write(randomBytes);

        return random.toByteArray();
    }
}

enum ContentType {
    change_cipher_spec(20), alert(21), handshake(22), application_data(23);

    private final int number;
    ContentType(int number) {
        this.number = number;
    }

    int getMagicNumber() { return number; }
};