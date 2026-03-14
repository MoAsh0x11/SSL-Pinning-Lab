package com.hacking.sslpinninglab.network;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;

import javax.net.ssl.SSLPeerUnverifiedException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class CertificateInterceptor implements Interceptor {
    // The expected SHA-256 hash of the public key (Base64 encoded)
    private static final String PINNED_HASH = "2jOcxjeYdyYgEbe5WeORT5/nBtRXXZBZMRherlhXZ4c=";

    @Override
    public Response intercept(Chain chain) throws IOException {
        okhttp3.Connection connection = chain.connection();
        
        if (connection != null && connection.handshake() != null) {
            List<Certificate> certificates = connection.handshake().peerCertificates();
            
            if (!certificates.isEmpty()) {
                X509Certificate cert = (X509Certificate) certificates.get(0);
                
                try {
                    byte[] publicKey = cert.getPublicKey().getEncoded();
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] hash = digest.digest(publicKey);
                    String hashString = Base64.getEncoder().encodeToString(hash);

                    if (!PINNED_HASH.equals(hashString)) {
                        throw new SSLPeerUnverifiedException("Interceptor Pinning failed: Public key hash mismatch!");
                    }
                } catch (NoSuchAlgorithmException e) {
                    throw new IOException("Failed to calculate certificate hash", e);
                }
            } else {
                throw new SSLPeerUnverifiedException("No peer certificates found.");
            }
        }
        
        return chain.proceed(chain.request());
    }
}