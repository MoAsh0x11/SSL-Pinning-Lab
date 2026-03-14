package com.hacking.sslpinninglab.network;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.net.ssl.X509TrustManager;
public class CustomTrustManager implements X509TrustManager {
    private static final String PINNED_KEY = "sha256/2jOcxjeYdyYgEbe5WeORT5/nBtRXXZBZMRherlhXZ4c=";
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {

        try {

            X509Certificate cert = chain[0];

            byte[] publicKey = cert.getPublicKey().getEncoded();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(publicKey);

            NativeVerifier verifier = new NativeVerifier();

            boolean valid = verifier.verifyPublicKey(publicKey);

            if (!valid) {
                throw new CertificateException("Native pinning failed");
            }

            String calculatedPin = "sha256/" +
                    Base64.getEncoder().encodeToString(hash);

            if (!calculatedPin.equals(PINNED_KEY)) {

                throw new CertificateException("Public key pin mismatch");

            }

        } catch (NoSuchAlgorithmException e) {

            throw new CertificateException(e);

        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {

        return new X509Certificate[]{};

    }

}
