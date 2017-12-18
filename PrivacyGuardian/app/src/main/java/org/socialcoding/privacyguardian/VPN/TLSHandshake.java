package org.socialcoding.privacyguardian.VPN;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by Hyun on 2017-12-08.
 */

public class TLSHandshake {
    public TLSHandshake(){};

    public SecurityParameters execute(FileInputStream in, FileOutputStream out, IPHeader ipHeader, TCPHeader tcpHeader) {
        SecurityParameters sp = null;
        return sp;
    }
}
