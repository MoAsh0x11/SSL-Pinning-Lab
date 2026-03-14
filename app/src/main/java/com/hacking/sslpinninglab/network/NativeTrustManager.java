package com.hacking.sslpinninglab.network;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public class NativeTrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {}

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        X509Certificate cert = chain[0];
        byte[] publicKey = cert.getPublicKey().getEncoded();

        NativeVerifier verifier = new NativeVerifier();
        boolean valid = verifier.verifyPublicKey(publicKey);

        if (!valid) {
            throw new CertificateException("Native pinning failed");
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[]{};
    }
}