package org.socialcoding.privacyguardian.VPN;

/**
 * Created by user on 2017-03-11.
 */

public class ICMPHeader {
    private int type;
    private int code;
    private int checksum;
    private int length;
    private byte[] header;

    ICMPHeader() {

    }

    ICMPHeader(byte[] packet, int ihl) {
        this.length = packet.length - ihl;
        System.out.println("ICMP Length: " + length);
        header = new byte[length];
        System.arraycopy(packet, ihl, header, 0, length);

        type = header[0] & 0xff;
        code = header[1] & 0xff;
    }

    int getType() { return type; }
    int getCode() { return code; }
}
