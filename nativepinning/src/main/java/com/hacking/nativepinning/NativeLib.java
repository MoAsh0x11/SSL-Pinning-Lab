package com.hacking.nativepinning;

public class NativeLib {

    // Used to load the 'nativepinning' library on application startup.
    static {
        System.loadLibrary("nativepinning");
    }

    /**
     * A native method that is implemented by the 'nativepinning' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}