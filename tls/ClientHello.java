package org.socialcoding.privacyguardian.VPN;

import java.nio.ByteBuffer;

/**
 * Created by HWY on 2017-07-11.
 */

public class ClientHello extends Handshake{
    ProtocolVersion clientVersion;
    Random random;
    SessionID sessionID;
    CipherSuite ciphersuites;
}


