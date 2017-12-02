package org.socialcoding.privacyguardian.Credential;

import android.util.Log;

import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.BasicConstraints;
import org.spongycastle.cert.CertIOException;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by disxc on 2017-10-13.
 */

public class CredentialManager {
    //TODO: CREATE appropriate interface for Credential manager.
    private static final String TAG = "CredentialManager";

    static public X509Certificate getRootCert(KeyPair keyPair) {
        X509Certificate selfCa;
        try {
            selfCa = generateSelfSigendCertificate(keyPair);
        } catch (Exception e) {
            Log.d(TAG, "error while creating selfsigned certificate");
            e.printStackTrace();
            return null;
        }
        Log.d(TAG, "successfully created certificate!");

        return selfCa;
    }

    static public boolean removeRootCA() {
        return false;
    }

    static public X509Certificate generateClonedCertificate(X509Certificate original, PrivateKey privateKey) {
        int version = original.getVersion();

        X500Name dnName = (X500Name) original.getSubjectDN();
        BigInteger serial = original.getSerialNumber();
        Date validAfter = original.getNotAfter();
        Date validUntil = original.getNotBefore();
        try {
            ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(privateKey);
        } catch (OperatorCreationException e) {
            e.printStackTrace();
        }

        return null;
    }

    // generates self-signed cert
    static private X509Certificate generateSelfSigendCertificate(KeyPair keyPair)
            throws NoSuchProviderException, NoSuchAlgorithmException,
            OperatorCreationException, CertIOException, CertificateException {
        final String subjectDN = "CN=PrivacyGuardian, C=KR";
        long now = System.currentTimeMillis();
        Date startDate = new Date(now);
        X500Name dnName = new X500Name(subjectDN);
        BigInteger certSerialNumber = new BigInteger(Long.toString(now)); // <-- Using the current timestamp as the certificate serial number

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.YEAR, 1); // <-- 1 Yr validity

        Date endDate = calendar.getTime();

        String signatureAlgorithm = "SHA256WithRSA"; // <-- Use appropriate signature algorithm based on your keyPair algorithm.

        ContentSigner contentSigner =
                new JcaContentSignerBuilder(signatureAlgorithm).build(keyPair.getPrivate());

        JcaX509v3CertificateBuilder certBuilder =
                new JcaX509v3CertificateBuilder(dnName,
                        certSerialNumber, startDate, endDate, dnName, keyPair.getPublic());

        // Extensions --------------------------

        // Basic Constraints
        BasicConstraints basicConstraints = new BasicConstraints(true); // <-- true for CA, false for EndEntity

        certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints); // Basic Constraints is usually marked as critical.

        // -------------------------------------

        return new JcaX509CertificateConverter().setProvider(
                        new BouncyCastleProvider()).getCertificate(certBuilder.build(contentSigner));
    }

}
