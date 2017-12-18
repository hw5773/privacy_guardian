package org.socialcoding.privacyguardian.VPN;

import java.security.cert.X509Certificate;
import java.util.Random;

/**
 * Created by HWY on 2017-07-11.
 */

public class SecurityParameters {
    private ConnectionEnd entity;
    private PRFAlgorithm prfAlgorithm;
    private BulkCipiherAlgorithm bulkCipiherAlgorithm;
    private CipherSuite cs;
    private byte encKeyLength;
    private byte blockLength;
    private byte fixedIVLength;
    private byte recordIVLength;
    private MACAlgorithm macAlgorithm;
    private byte macLength;
    private byte macKeyLength;
    private CompressionMethod compressionAlgorithm;
    private byte[] masterSecret;
    private byte[] clientRandom;
    private byte[] serverRandom;

    public SecurityParameters() {
        this.entity = ConnectionEnd.server;
        cs = new CipherSuite();
    }

    public void setClientRandom(byte[] cr) {
        clientRandom = cr;
    }

    public void setServerRandom(byte[] sr) {
        serverRandom = sr;
    }

    public void setMasterSecret(byte[] ms) {
        this.masterSecret = ms;
    }

    public void setBulkCipiherAlgorithm(BulkCipiherAlgorithm cipiherAlgorithm) {
        this.bulkCipiherAlgorithm = cipiherAlgorithm;

        switch (cipiherAlgorithm) {
            case aes128:
                encKeyLength = 16;
                fixedIVLength = 16;
                recordIVLength = 16;
                blockLength = 16;
                break;
            case aes256:
                encKeyLength = 32;
                fixedIVLength = 16;
                recordIVLength = 16;
                blockLength = 16;
                break;
            default:
                encKeyLength = 0;
                fixedIVLength = 0;
                recordIVLength = 0;
                blockLength = 0;
        }
    }

    public void setMacAlgorithm(MACAlgorithm macAlgorithm) {
        this.macAlgorithm = macAlgorithm;

        switch (macAlgorithm) {
            case md5:
                macLength = 16;
                macKeyLength = 16;
                break;
            case sha1:
                macLength = 20;
                macKeyLength = 20;
                break;
            case sha256:
                macLength = 32;
                macKeyLength = 32;
                break;
            default:
                macLength = 0;
                macKeyLength = 0;
        }
    }

    public CipherSuite getCipherSuite() {
        return cs;
    }
    public BulkCipiherAlgorithm getBulkCipiherAlgorithm() {
        return bulkCipiherAlgorithm;
    }
    public MACAlgorithm getMacAlgorithm() {
        return macAlgorithm;
    }
    public byte[] getClientRandom() {
        return clientRandom;
    }
    public byte[] getServerRandom() {
        return serverRandom;
    }
    public byte[] getMasterSecret() {
        return masterSecret;
    }
    public byte getEncKeyLength() {
        return encKeyLength;
    }
    public byte getFixedIVLength() {
        return fixedIVLength;
    }
    public byte getRecordIVLength() {
        return recordIVLength;
    }
    public byte getBlockLength() {
        return blockLength;
    }
    public byte getMacLength() {
        return macLength;
    }
    public byte getMacKeyLength() {
        return macKeyLength;
    }
}

enum ConnectionEnd {
    server, client
};

enum PRFAlgorithm {
    tls_prf_sha256
};

enum BulkCipiherAlgorithm {
    none, rc4, tdes, aes128, aes256, aes128ccm, aes256ccm, aes128gcm, aes256gcm
};

enum CipherType {
    stream, block, aead
};

enum MACAlgorithm { none, md5, sha1, sha256 };

enum CompressionMethod {
    none(0), all(255);

    private final int number;
    CompressionMethod(int number) {
        this.number = number;
    }

    int getMagicNumber() { return number; }
};
