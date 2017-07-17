package org.socialcoding.privacyguardian.VPN;

/**
 * Created by HWY on 2017-07-11.
 */

public class TLSPlaintext extends Record {
    private byte[] plaintext;

    public TLSPlaintext() {};
    public TLSPlaintext(ContentType type, ProtocolVersion version, short length, byte[] plaintext) {
        super(type, version, length);
        this.plaintext = plaintext;
    }

    public void setPlaintext(byte[] plaintext) { this.plaintext = plaintext; }
    public byte[] getPlaintext() { return plaintext; }
}
