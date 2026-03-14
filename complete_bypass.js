Java.perform(function () {
    console.log("\n[*] Starting Complete SSL Pinning Bypass (OkHttp 4 Kotlin + Universal Signatures)...");

    // 1. Bypass Custom TrustManagers
    var trustManagerClasses = [
        "com.hacking.sslpinninglab.network.CustomTrustManager",
        "com.hacking.sslpinninglab.network.NativeTrustManager",
        "com.hacking.sslpinninglab.network.CtTrustManager"
    ];

    trustManagerClasses.forEach(function (className) {
        try {
            var TargetClass = Java.use(className);
            var overloads = TargetClass.checkServerTrusted.overloads;
            for (var i = 0; i < overloads.length; i++) {
                overloads[i].implementation = function () {
                    console.log("[+] Bypassing " + className + ".checkServerTrusted()");
                    return; 
                };
            }
            console.log("[!] Successfully hooked: " + className);
        } catch (err) {
            console.log("[-] Failed to hook " + className + ": " + err);
        }
    });

    // 2. Bypass Network Security Config (NSC) directly via Android internals
    try {
        var NSC = Java.use("android.security.net.config.NetworkSecurityTrustManager");
        var nscOverloads = NSC.checkServerTrusted.overloads;
        for (var j = 0; j < nscOverloads.length; j++) {
            nscOverloads[j].implementation = function() {
                console.log("[+] Bypassed Android NetworkSecurityConfig (NSC)");
                return arguments[0]; // Usually returns the cert chain
            };
        }
        console.log("[!] Successfully hooked Android NetworkSecurityTrustManager (NSC).");
    } catch(err) {
        console.log("[-] Error hooking NetworkSecurityTrustManager: " + err);
    }

    // 3. Fallback for NSC: Conscrypt (Often used on Android 7+)
    try {
        var TrustManagerImpl = Java.use("com.android.org.conscrypt.TrustManagerImpl");
        var tcOverloads = TrustManagerImpl.checkServerTrusted.overloads;
        for (var k = 0; k < tcOverloads.length; k++) {
            tcOverloads[k].implementation = function() {
                console.log("[+] Bypassed Conscrypt System TrustManager (NSC fallback)");
                return arguments[0]; // Return the chain array
            };
        }
        console.log("[!] Successfully hooked Conscrypt TrustManagerImpl (NSC Fallback).");
    } catch(err) {
        console.log("[-] Error hooking Conscrypt TrustManagerImpl: " + err);
    }

    // 4. Bypass OkHttp CertificatePinner (Supports v3 Java and v4 Kotlin)
    try {
        var CertificatePinner = Java.use('okhttp3.CertificatePinner');
        
        // Hook all overloads of the standard 'check' method (Legacy / Public API)
        if (CertificatePinner.check) {
            var cpOverloads = CertificatePinner.check.overloads;
            for (var i = 0; i < cpOverloads.length; i++) {
                cpOverloads[i].implementation = function() {
                    console.log("[+] Bypassed OkHttp CertificatePinner.check() [Overload " + i + "]");
                    return; // Return nothing to indicate trust
                };
            }
        }

        // Hook the OkHttp 4 Kotlin-specific internal method 'check$okhttp'
        // In Kotlin, internal methods are often mangled with the module name.
        if (CertificatePinner.check$okhttp) {
            var kotlinOverloads = CertificatePinner.check$okhttp.overloads;
            for (var i = 0; i < kotlinOverloads.length; i++) {
                kotlinOverloads[i].implementation = function() {
                    console.log("[+] Bypassed OkHttp CertificatePinner.check$okhttp() (Kotlin Internal)");
                    return; 
                };
            }
        }

        // Additional OkHttp 4 Bypass: Neutralize findMatchingPins
        // If it can't find pins for the host, it skips checking entirely!
        if (CertificatePinner.findMatchingPins) {
            var fmpOverloads = CertificatePinner.findMatchingPins.overloads;
            for (var i = 0; i < fmpOverloads.length; i++) {
                fmpOverloads[i].implementation = function() {
                    console.log("[+] Bypassed OkHttp CertificatePinner.findMatchingPins()");
                    return Java.use("java.util.Collections").emptyList(); // Return empty list
                };
            }
        }

        console.log("[!] Successfully hooked ALL OkHttp CertificatePinner logic (v3 & v4 Kotlin).");
    } catch(err) {
        console.log("[-] Error hooking CertificatePinner: " + err);
    }

    // 5. Bypass Custom OkHttp Network Interceptor
    try {
        var CertificateInterceptor = Java.use("com.hacking.sslpinninglab.network.CertificateInterceptor");
        var interceptOverloads = CertificateInterceptor.intercept.overloads;
        for (var i = 0; i < interceptOverloads.length; i++) {
            interceptOverloads[i].implementation = function (chain) {
                console.log("[+] Bypassing Custom OkHttp CertificateInterceptor");
                return chain.proceed(chain.request());
            };
        }
        console.log("[!] Successfully hooked OkHttp CertificateInterceptor");
    } catch (err) {
        console.log("[-] Failed to hook CertificateInterceptor: " + err);
    }
});