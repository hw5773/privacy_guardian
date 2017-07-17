package org.socialcoding.privacyguardian.VPN;

import java.security.cert.X509Certificate;
import java.util.Random;

/**
 * Created by HWY on 2017-07-11.
 */

public class SecurityParameters {
    private long sessionID;
    private X509Certificate peerCertificate;
    private short compressionMethod;
    private short cipherSpec;
    private byte[] masterSecret;
    private boolean isResumable = false;

    public SecurityParameters() {
    }
}
