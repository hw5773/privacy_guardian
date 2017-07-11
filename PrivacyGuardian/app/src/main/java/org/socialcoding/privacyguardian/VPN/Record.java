package org.socialcoding.privacyguardian.VPN;

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

enum ContentType {
    change_cipher_spec(20), alert(21), handshake(22), application_data(23);

    private final int number;
    ContentType(int number) {
        this.number = number;
    }

    int getMagicNumber() { return number; }
};