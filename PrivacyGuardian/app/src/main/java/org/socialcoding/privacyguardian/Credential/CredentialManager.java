package org.socialcoding.privacyguardian.Credential;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
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
    public static final String ROOT_KEY_ENTRY_ALIAS = "PrivacyGuardian";

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
    private static X509Certificate generateSelfSignedCertificate(KeyPair keyPair)
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


    /* pasted from https://devops.datenkollektiv.de/how-to-create
    -an-android-keystore-with-bouncy-castle.html */

    public static KeyStore loadKeystore(byte[] keystoreBytes, String password)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore ks;
        char[] pass = password.toCharArray();
        ks = KeyStore.getInstance("BouncyCastle");
        if (keystoreBytes != null) {
            ks.load(new ByteArrayInputStream(keystoreBytes), pass);
        } else {
            ks.load(null);
        }
        return ks;
    }

    // generates RootCertKeystore and returns keystore
    public static byte[] generateRootCertKeystore(String password) {
        KeyStore ks;
        try {
            ks = loadKeystore(null, password);
            KeyPair keyPair = generateKeyPair();
            X509Certificate certificate = generateSelfSignedCertificate(keyPair);
            ks.setKeyEntry(ROOT_KEY_ENTRY_ALIAS,
                    keyPair.getPrivate(),
                    password.toCharArray(),
                    new X509Certificate[]{
                            certificate
                    });
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ks.store(os, password.toCharArray());
            return os.toByteArray();
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException
                | IOException | NoSuchProviderException | OperatorCreationException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Failed to generate default Android debug keystore.");
    }

    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
        return keyGen.generateKeyPair();
    }

    public static void writeKeystoreFile(KeyStore ks, FileOutputStream outputStream, String password)
            throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        ks.store(outputStream, password.toCharArray());
    }

    public static KeyStore readKeystoreFromFile(FileInputStream inputStream, String password)
            throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        inputStream.close();
        return loadKeystore(bytes, password);
    }
}
