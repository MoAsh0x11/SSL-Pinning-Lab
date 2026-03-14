Java.perform(function () {
    console.log("\n[*] Starting Complete SSL Pinning Bypass...");

    // 1. Bypass Custom TrustManagers
    var trustManagerClasses = [
        "com.hacking.sslpinninglab.network.CustomTrustManager",
        "com.hacking.sslpinninglab.network.NativeTrustManager",
        "com.hacking.sslpinninglab.network.CtTrustManager"
    ];

    trustManagerClasses.forEach(function (className) {
        try {
            var TargetClass = Java.use(className);
            TargetClass.checkServerTrusted.overload('[Ljava.security.cert.X509Certificate;', 'java.lang.String').implementation = function (chain, authType) {
                console.log("[+] Bypassing " + className + ".checkServerTrusted()");
                return; 
            };
            console.log("[!] Successfully hooked: " + className);
        } catch (err) {
            console.log("[-] Failed to hook " + className + ": " + err);
        }
    });

    // 2. Bypass the default System TrustManager (NSC)
    try {
        var TrustManagerImpl = Java.use("com.android.org.conscrypt.TrustManagerImpl");
        TrustManagerImpl.checkServerTrusted.overload(
            '[Ljava.security.cert.X509Certificate;', 
            'java.lang.String', 
            'java.lang.String'
        ).implementation = function (chain, authType, host) {
            console.log("[+] Bypassing System NSC for host: " + host);
            return chain; 
        };
        console.log("[!] Successfully hooked System TrustManager (NSC)");
    } catch (err) {
        console.log("[-] Conscrypt TrustManager not found");
    }

    // 3. Bypass OkHttp CertificatePinner
    try {
        var CertificatePinner = Java.use("okhttp3.CertificatePinner");
        CertificatePinner.check.overload('java.lang.String', 'java.util.List').implementation = function (hostname, peerCertificates) {
            console.log("[+] Bypassing OkHttp CertificatePinner for: " + hostname);
            return;
        };
        console.log("[!] Successfully hooked OkHttp CertificatePinner");
    } catch (err) {
        console.log("[-] Failed to hook OkHttp CertificatePinner: " + err);
    }

    // 4. Bypass OkHttp Network Interceptor
    try {
        var CertificateInterceptor = Java.use("com.hacking.sslpinninglab.network.CertificateInterceptor");
        CertificateInterceptor.intercept.overload('okhttp3.Interceptor$Chain').implementation = function (chain) {
            console.log("[+] Bypassing Custom OkHttp CertificateInterceptor");
            // Just immediately proceed with the request, ignoring the certificate checks
            return chain.proceed(chain.request());
        };
        console.log("[!] Successfully hooked OkHttp CertificateInterceptor");
    } catch (err) {
        console.log("[-] Failed to hook CertificateInterceptor: " + err);
    }
});