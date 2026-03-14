package com.hacking.sslpinninglab.network;

public class NativeVerifier {
    static {
        System.loadLibrary("nativepinning");
    }

    public native boolean verifyPublicKey(byte[] publicKey);
}
