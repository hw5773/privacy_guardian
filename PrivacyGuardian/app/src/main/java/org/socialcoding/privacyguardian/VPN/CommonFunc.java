package org.socialcoding.privacyguardian.VPN;

/**
 * Created by Hyun on 2017-12-01.
 */

public class CommonFunc {
    public static byte[] lengthToBytes(int length) {
        byte[] ret = new byte[3];
        ret[0] = (byte) ((length & 0x00ff0000) >> 16);
        ret[1] = (byte) ((length & 0x0000ff00) >> 8);
        ret[2] = (byte) (length & 0x000000ff);

        return ret;
    }
}