Java.perform(function() {
    console.log("\n[*] Searching for correct OkHttp and NSC signatures...");

    // 1. OkHttp CertificatePinner
    try {
        var CertificatePinner = Java.use('okhttp3.CertificatePinner');
        
        // Find all overloads of check
        var overloads = CertificatePinner.check.overloads;
        console.log("[i] Found " + overloads.length + " overloads for okhttp3.CertificatePinner.check()");
        
        for (var i = 0; i < overloads.length; i++) {
            var args = overloads[i].argumentTypes.map(function(t) { return t.className; });
            console.log("    -> Overload " + i + " args: [" + args.join(', ') + "]");
            
            // Hook all overloads
            overloads[i].implementation = function() {
                console.log("[+] Bypassed OkHttp CertificatePinner.check() (Universal)");
                return; // Return nothing, meaning it's trusted
            };
        }
        console.log("[!] Successfully hooked ALL OkHttp CertificatePinner overloads.");
    } catch(err) {
        console.log("[-] Error hooking CertificatePinner: " + err);
    }
    
    // 2. Android Network Security Config (NSC) directly via Android internals
    try {
        var NSC = Java.use("android.security.net.config.NetworkSecurityTrustManager");
        var nscOverloads = NSC.checkServerTrusted.overloads;
        console.log("[i] Found " + nscOverloads.length + " overloads for NetworkSecurityTrustManager.checkServerTrusted()");
        
        for (var j = 0; j < nscOverloads.length; j++) {
            var args = nscOverloads[j].argumentTypes.map(function(t) { return t.className; });
            console.log("    -> Overload " + j + " args: [" + args.join(', ') + "]");
            
            nscOverloads[j].implementation = function() {
                console.log("[+] Bypassed Android NetworkSecurityConfig (NSC)");
                return arguments[0]; // Usually returns the cert chain or null/void
            };
        }
        console.log("[!] Successfully hooked Android NetworkSecurityTrustManager.");
    } catch(err) {
        console.log("[-] Error hooking NetworkSecurityTrustManager (Device might use Conscrypt): " + err);
    }
    
    // 3. Fallback for NSC: Conscrypt (Often used on Android 7+)
    try {
        var TrustManagerImpl = Java.use("com.android.org.conscrypt.TrustManagerImpl");
        var tcOverloads = TrustManagerImpl.checkServerTrusted.overloads;
        console.log("[i] Found " + tcOverloads.length + " overloads for Conscrypt TrustManagerImpl.checkServerTrusted()");
        
        for (var k = 0; k < tcOverloads.length; k++) {
            var args = tcOverloads[k].argumentTypes.map(function(t) { return t.className; });
            console.log("    -> Overload " + k + " args: [" + args.join(', ') + "]");
            
            tcOverloads[k].implementation = function() {
                console.log("[+] Bypassed Conscrypt System TrustManager (NSC fallback)");
                return arguments[0]; // Return the chain array
            };
        }
        console.log("[!] Successfully hooked Conscrypt TrustManagerImpl.");
    } catch(err) {
        console.log("[-] Error hooking Conscrypt TrustManagerImpl: " + err);
    }
});