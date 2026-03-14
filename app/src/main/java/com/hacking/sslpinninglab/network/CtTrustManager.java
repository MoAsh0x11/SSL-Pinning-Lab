package com.hacking.sslpinninglab.network;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public class CtTrustManager implements X509TrustManager {
    // OID for Certificate Transparency Signed Certificate Timestamps (SCTs)
    private static final String CT_OID = "1.3.6.1.4.1.11129.2.4.2";

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {}

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (chain == null || chain.length == 0) {
            throw new CertificateException("Certificate chain is empty");
        }
        
        X509Certificate cert = chain[0];
        
        // Simple CT verification: check if the certificate has the CT extension
        // A real implementation would parse the SCT list and verify signatures against known CT Log servers.
        byte[] ctExtension = cert.getExtensionValue(CT_OID);
        
        if (ctExtension == null) {
            throw new CertificateException("Certificate Transparency (CT) extension missing! Possible interception.");
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[]{};
    }
}