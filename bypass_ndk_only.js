Java.perform(function () {
    console.log("\n[*] Starting Isolated Native NDK Bypass...");

    // The name of the compiled C++ library
    var moduleName = "libnativepinning.so";
    
    // The exact exported JNI function name
    var functionName = "Java_com_hacking_sslpinninglab_network_NativeVerifier_verifyPublicKey";

    // Wait for the library to be loaded into memory
    var waitForModule = setInterval(function() {
        var module = Process.findModuleByName(moduleName);
        if (module) {
            clearInterval(waitForModule);
            console.log("[+] Found " + moduleName + " in memory!");
            
            var targetAddress = Module.findExportByName(moduleName, functionName);
            if (targetAddress) {
                Interceptor.attach(targetAddress, {
                    onEnter: function (args) {
                        console.log("\n[+] Intercepted C++ verifyPublicKey()!");
                    },
                    onLeave: function (retval) {
                        console.log("[-] Original Return Value: " + retval);
                        // JNI_TRUE is 1, JNI_FALSE is 0
                        // We force it to return 1 to bypass the pinning check!
                        retval.replace(1);
                        console.log("[+] Forced Return Value to 1 (Bypassed!)");
                    }
                });
                console.log("[!] Successfully hooked native function: " + functionName);
            } else {
                console.log("[-] Could not find export: " + functionName);
            }
        }
    }, 100); // Check every 100ms
});