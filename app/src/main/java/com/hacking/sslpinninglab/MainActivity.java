package com.hacking.sslpinninglab;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hacking.sslpinninglab.network.CustomTrustManager;
import com.hacking.sslpinninglab.network.NativeTrustManager;
import com.hacking.sslpinninglab.network.CtTrustManager;
import com.hacking.sslpinninglab.network.CertificateInterceptor;

import java.io.IOException;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {

    private OkHttpClient customClient;
    private OkHttpClient nativeClient;
    private OkHttpClient ctClient;
    private OkHttpClient interceptorClient;
    private OkHttpClient defaultClient; // Uses System TrustManager, so NSC applies
    private OkHttpClient permissiveClient; // For when NSC is "disabled" internally
    
    // Internal intent states
    private boolean isCustomTrustEnabled = true;
    private boolean isOkHttpPinnerEnabled = true;
    private boolean isNscEnabled = true;
    private boolean isNativeEnabled = true;
    private boolean isCtEnabled = true;
    private boolean isInterceptorEnabled = true;

    // Detected bypass states
    private boolean isCustomBypassed = false;
    private boolean isOkHttpPinnerBypassed = false;
    private boolean isNscBypassed = false;
    private boolean isNativeBypassed = false;
    private boolean isCtBypassed = false;
    private boolean isInterceptorBypassed = false;

    private Button btnCustomTrust;
    private Button btnOkHttpPinner;
    private Button btnNsc;
    private Button btnNativePinning;
    private Button btnCt;
    private Button btnInterceptor;

    private boolean isChecking = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupClients();

        btnCustomTrust = findViewById(R.id.btnCustomTrust);
        btnOkHttpPinner = findViewById(R.id.btnOkHttpPinner);
        btnNsc = findViewById(R.id.btnNsc);
        btnNativePinning = findViewById(R.id.btnNativePinning);
        btnCt = findViewById(R.id.btnCt);
        btnInterceptor = findViewById(R.id.btnInterceptor);

        // Short click: Send the actual requests to the target server
        btnCustomTrust.setOnClickListener(v -> performCustomTrustRequest());
        btnOkHttpPinner.setOnClickListener(v -> performOkHttpPinnerRequest());
        btnNsc.setOnClickListener(v -> performNscRequest());
        btnNativePinning.setOnClickListener(v -> performNativeRequest());
        btnCt.setOnClickListener(v -> performCtRequest());
        btnInterceptor.setOnClickListener(v -> performInterceptorRequest());

        // Long click: Toggle internal enable/disable state
        btnCustomTrust.setOnLongClickListener(v -> {
            isCustomTrustEnabled = !isCustomTrustEnabled;
            updateUI();
            return true;
        });

        btnOkHttpPinner.setOnLongClickListener(v -> {
            isOkHttpPinnerEnabled = !isOkHttpPinnerEnabled;
            updateUI();
            return true;
        });

        btnNsc.setOnLongClickListener(v -> {
            isNscEnabled = !isNscEnabled;
            updateUI();
            return true;
        });

        btnNativePinning.setOnLongClickListener(v -> {
            isNativeEnabled = !isNativeEnabled;
            updateUI();
            return true;
        });

        btnCt.setOnLongClickListener(v -> {
            isCtEnabled = !isCtEnabled;
            updateUI();
            return true;
        });

        btnInterceptor.setOnLongClickListener(v -> {
            isInterceptorEnabled = !isInterceptorEnabled;
            updateUI();
            return true;
        });

        // Start background bypass detection
        startBypassDetection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isChecking = false; // Stop the background thread
    }

    private void updateUI() {
        runOnUiThread(() -> {
            boolean customWorking = isCustomTrustEnabled && !isCustomBypassed;
            boolean pinnerWorking = isOkHttpPinnerEnabled && !isOkHttpPinnerBypassed;
            boolean nscWorking = isNscEnabled && !isNscBypassed;
            boolean nativeWorking = isNativeEnabled && !isNativeBypassed;
            boolean ctWorking = isCtEnabled && !isCtBypassed;
            boolean interceptorWorking = isInterceptorEnabled && !isInterceptorBypassed;

            int colorSecure = Color.parseColor("#139090");
            int colorBypassed = Color.parseColor("#E56B55");

            try {
                android.graphics.drawable.LayerDrawable customBg = (android.graphics.drawable.LayerDrawable) btnCustomTrust.getBackground();
                android.graphics.drawable.GradientDrawable customMain = (android.graphics.drawable.GradientDrawable) customBg.findDrawableByLayerId(R.id.main_body);
                customMain.setColor(customWorking ? colorSecure : colorBypassed);

                android.graphics.drawable.LayerDrawable pinnerBg = (android.graphics.drawable.LayerDrawable) btnOkHttpPinner.getBackground();
                android.graphics.drawable.GradientDrawable pinnerMain = (android.graphics.drawable.GradientDrawable) pinnerBg.findDrawableByLayerId(R.id.main_body);
                pinnerMain.setColor(pinnerWorking ? colorSecure : colorBypassed);
                
                android.graphics.drawable.LayerDrawable nscBg = (android.graphics.drawable.LayerDrawable) btnNsc.getBackground();
                android.graphics.drawable.GradientDrawable nscMain = (android.graphics.drawable.GradientDrawable) nscBg.findDrawableByLayerId(R.id.main_body);
                nscMain.setColor(nscWorking ? colorSecure : colorBypassed);

                android.graphics.drawable.LayerDrawable nativeBg = (android.graphics.drawable.LayerDrawable) btnNativePinning.getBackground();
                android.graphics.drawable.GradientDrawable nativeMain = (android.graphics.drawable.GradientDrawable) nativeBg.findDrawableByLayerId(R.id.main_body);
                nativeMain.setColor(nativeWorking ? colorSecure : colorBypassed);

                android.graphics.drawable.LayerDrawable ctBg = (android.graphics.drawable.LayerDrawable) btnCt.getBackground();
                android.graphics.drawable.GradientDrawable ctMain = (android.graphics.drawable.GradientDrawable) ctBg.findDrawableByLayerId(R.id.main_body);
                ctMain.setColor(ctWorking ? colorSecure : colorBypassed);

                android.graphics.drawable.LayerDrawable interceptorBg = (android.graphics.drawable.LayerDrawable) btnInterceptor.getBackground();
                android.graphics.drawable.GradientDrawable interceptorMain = (android.graphics.drawable.GradientDrawable) interceptorBg.findDrawableByLayerId(R.id.main_body);
                interceptorMain.setColor(interceptorWorking ? colorSecure : colorBypassed);
            } catch (Exception e) {
                e.printStackTrace();
            }

            btnCustomTrust.setText("CUSTOM TRUST MANAGER\n" + (customWorking ? "[ SECURE ]" : "[ BYPASSED ]"));
            btnOkHttpPinner.setText("OKHTTP PINNER\n" + (pinnerWorking ? "[ SECURE ]" : "[ BYPASSED ]"));
            btnNsc.setText("NETWORK SECURITY CONFIG\n" + (nscWorking ? "[ SECURE ]" : "[ BYPASSED ]"));
            btnNativePinning.setText("NATIVE NDK PINNING\n" + (nativeWorking ? "[ SECURE ]" : "[ BYPASSED ]"));
            btnCt.setText("CERTIFICATE TRANSPARENCY\n" + (ctWorking ? "[ SECURE ]" : "[ BYPASSED ]"));
            btnInterceptor.setText("OKHTTP INTERCEPTOR\n" + (interceptorWorking ? "[ SECURE ]" : "[ BYPASSED ]"));
        });
    }

    private void startBypassDetection() {
        new Thread(() -> {
            while (isChecking) {
                checkCustomTrustBypass();
                checkOkHttpPinnerBypass();
                checkNscBypass();
                checkNativeBypass();
                checkCtBypass();
                checkInterceptorBypass();
                updateUI();
                
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void checkCustomTrustBypass() {
        try {
            String fakeCertStr = "-----BEGIN CERTIFICATE-----\n" +
                    "MIIC/zCCAeegAwIBAgIUJTHclo2v9id1sZug56Clue68WP4wDQYJKoZIhvcNAQEL\n" +
                    "BQAwDzENMAsGA1UEAwwEZmFrZTAeFw0yNjAzMTQwMTIwNTlaFw0yNzAzMTQwMTIw\n" +
                    "NTlaMA8xDTALBgNVBAMMBGZha2UwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEK\n" +
                    "AoIBAQDF/HxvGZ0i+ubhQzl1YkRNKhxm4kB2ISgfjAs4Jiug2jeDl1nnInTgJc9M\n" +
                    "RDaje03I5zqkFttA3Par1W6xyI0FWXJ+hFsUVveDhZbjfg4+qbrO76nu8/76DTiK\n" +
                    "hTfclkzhZgnhEnUTBGikuqzrorg2jXuDbIvl95q5OroX86twFx1CDmzhB8YPvu6p\n" +
                    "wwprxjiutUplVT1sLosQxxG176gVFJmOK9B8DkmmX3OfD4yMBjiWEEzWLTmKMa9v\n" +
                    "y/Uwh0fPmZyYzOBGfRX92g73AHTpd61twLAqI8GkFQciO9n8WnaSbBUYPGHHCZkP\n" +
                    "226p3z5HiVsDP6Sp3Fn2B2sk3caFAgMBAAGjUzBRMB0GA1UdDgQWBBSrEfFdKd9d\n" +
                    "rgt3Or0emcwrOBXwOzAfBgNVHSMEGDAWgBSrEfFdKd9drgt3Or0emcwrOBXwOzAP\n" +
                    "BgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQBYKCrQDdNop/hpze33\n" +
                    "7JpLGR5eSizeVBjTFF+QrTYwRpklWQHFj95ew6lReTXbW4CQUEiOphX3DoIn6Fn+\n" +
                    "ycJaTDTlPaMQfRap0uFhrnAMuL62VYZ36NVw/GPlCfSS+FQeEWoNzUFX/IoAjJDi\n" +
                    "ENEnxzUURtn5OIDuMKN0gTL+pshfYjQignUCfKnH0EWvKEAJn1pvPkLSXO7MiOID\n" +
                    "gKHuMYY7WfAH+5MeAcdBsMRgMhTbwnEiU2MMc7O7uAX1MJ8fsmnlLk8zyicwCixI\n" +
                    "/jm2YcyHsIQCKK86GIH0FDQnN35irdhRv0GnRYj2x/y8XnL2esSE3myHikgZTm0/\n" +
                    "Oc+O\n" +
                    "-----END CERTIFICATE-----";
            java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
            java.security.cert.X509Certificate fakeCert = (java.security.cert.X509Certificate) cf.generateCertificate(
                    new java.io.ByteArrayInputStream(fakeCertStr.getBytes())
            );
            CustomTrustManager tm = new CustomTrustManager();
            tm.checkServerTrusted(new java.security.cert.X509Certificate[]{fakeCert}, "RSA");
            isCustomBypassed = true;
        } catch (Exception e) {
            isCustomBypassed = false;
        }
    }

    private void checkNativeBypass() {
        try {
            String fakeCertStr = "-----BEGIN CERTIFICATE-----\n" +
                    "MIIC/zCCAeegAwIBAgIUJTHclo2v9id1sZug56Clue68WP4wDQYJKoZIhvcNAQEL\n" +
                    "BQAwDzENMAsGA1UEAwwEZmFrZTAeFw0yNjAzMTQwMTIwNTlaFw0yNzAzMTQwMTIw\n" +
                    "NTlaMA8xDTALBgNVBAMMBGZha2UwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEK\n" +
                    "AoIBAQDF/HxvGZ0i+ubhQzl1YkRNKhxm4kB2ISgfjAs4Jiug2jeDl1nnInTgJc9M\n" +
                    "RDaje03I5zqkFttA3Par1W6xyI0FWXJ+hFsUVveDhZbjfg4+qbrO76nu8/76DTiK\n" +
                    "hTfclkzhZgnhEnUTBGikuqzrorg2jXuDbIvl95q5OroX86twFx1CDmzhB8YPvu6p\n" +
                    "wwprxjiutUplVT1sLosQxxG176gVFJmOK9B8DkmmX3OfD4yMBjiWEEzWLTmKMa9v\n" +
                    "y/Uwh0fPmZyYzOBGfRX92g73AHTpd61twLAqI8GkFQciO9n8WnaSbBUYPGHHCZkP\n" +
                    "226p3z5HiVsDP6Sp3Fn2B2sk3caFAgMBAAGjUzBRMB0GA1UdDgQWBBSrEfFdKd9d\n" +
                    "rgt3Or0emcwrOBXwOzAfBgNVHSMEGDAWgBSrEfFdKd9drgt3Or0emcwrOBXwOzAP\n" +
                    "BgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQBYKCrQDdNop/hpze33\n" +
                    "7JpLGR5eSizeVBjTFF+QrTYwRpklWQHFj95ew6lReTXbW4CQUEiOphX3DoIn6Fn+\n" +
                    "ycJaTDTlPaMQfRap0uFhrnAMuL62VYZ36NVw/GPlCfSS+FQeEWoNzUFX/IoAjJDi\n" +
                    "ENEnxzUURtn5OIDuMKN0gTL+pshfYjQignUCfKnH0EWvKEAJn1pvPkLSXO7MiOID\n" +
                    "gKHuMYY7WfAH+5MeAcdBsMRgMhTbwnEiU2MMc7O7uAX1MJ8fsmnlLk8zyicwCixI\n" +
                    "/jm2YcyHsIQCKK86GIH0FDQnN35irdhRv0GnRYj2x/y8XnL2esSE3myHikgZTm0/\n" +
                    "Oc+O\n" +
                    "-----END CERTIFICATE-----";
            java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
            java.security.cert.X509Certificate fakeCert = (java.security.cert.X509Certificate) cf.generateCertificate(
                    new java.io.ByteArrayInputStream(fakeCertStr.getBytes())
            );
            NativeTrustManager tm = new NativeTrustManager();
            tm.checkServerTrusted(new java.security.cert.X509Certificate[]{fakeCert}, "RSA");
            isNativeBypassed = true;
        } catch (Exception e) {
            isNativeBypassed = false;
        }
    }

    private void checkCtBypass() {
        try {
            String fakeCertStr = "-----BEGIN CERTIFICATE-----\n" +
                    "MIIC/zCCAeegAwIBAgIUJTHclo2v9id1sZug56Clue68WP4wDQYJKoZIhvcNAQEL\n" +
                    "BQAwDzENMAsGA1UEAwwEZmFrZTAeFw0yNjAzMTQwMTIwNTlaFw0yNzAzMTQwMTIw\n" +
                    "NTlaMA8xDTALBgNVBAMMBGZha2UwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEK\n" +
                    "AoIBAQDF/HxvGZ0i+ubhQzl1YkRNKhxm4kB2ISgfjAs4Jiug2jeDl1nnInTgJc9M\n" +
                    "RDaje03I5zqkFttA3Par1W6xyI0FWXJ+hFsUVveDhZbjfg4+qbrO76nu8/76DTiK\n" +
                    "hTfclkzhZgnhEnUTBGikuqzrorg2jXuDbIvl95q5OroX86twFx1CDmzhB8YPvu6p\n" +
                    "wwprxjiutUplVT1sLosQxxG176gVFJmOK9B8DkmmX3OfD4yMBjiWEEzWLTmKMa9v\n" +
                    "y/Uwh0fPmZyYzOBGfRX92g73AHTpd61twLAqI8GkFQciO9n8WnaSbBUYPGHHCZkP\n" +
                    "226p3z5HiVsDP6Sp3Fn2B2sk3caFAgMBAAGjUzBRMB0GA1UdDgQWBBSrEfFdKd9d\n" +
                    "rgt3Or0emcwrOBXwOzAfBgNVHSMEGDAWgBSrEfFdKd9drgt3Or0emcwrOBXwOzAP\n" +
                    "BgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQBYKCrQDdNop/hpze33\n" +
                    "7JpLGR5eSizeVBjTFF+QrTYwRpklWQHFj95ew6lReTXbW4CQUEiOphX3DoIn6Fn+\n" +
                    "ycJaTDTlPaMQfRap0uFhrnAMuL62VYZ36NVw/GPlCfSS+FQeEWoNzUFX/IoAjJDi\n" +
                    "ENEnxzUURtn5OIDuMKN0gTL+pshfYjQignUCfKnH0EWvKEAJn1pvPkLSXO7MiOID\n" +
                    "gKHuMYY7WfAH+5MeAcdBsMRgMhTbwnEiU2MMc7O7uAX1MJ8fsmnlLk8zyicwCixI\n" +
                    "/jm2YcyHsIQCKK86GIH0FDQnN35irdhRv0GnRYj2x/y8XnL2esSE3myHikgZTm0/\n" +
                    "Oc+O\n" +
                    "-----END CERTIFICATE-----";
            java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
            java.security.cert.X509Certificate fakeCert = (java.security.cert.X509Certificate) cf.generateCertificate(
                    new java.io.ByteArrayInputStream(fakeCertStr.getBytes())
            );
            CtTrustManager tm = new CtTrustManager();
            tm.checkServerTrusted(new java.security.cert.X509Certificate[]{fakeCert}, "RSA");
            
            // If it reaches here without throwing an exception, it has been bypassed!
            isCtBypassed = true;
        } catch (Exception e) {
            // It correctly threw an exception, meaning it is secure
            isCtBypassed = false;
        }
    }

    private void checkOkHttpPinnerBypass() {
        CertificatePinner fakePinner = new CertificatePinner.Builder()
                .add("google.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
                .build();
                
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .certificatePinner(fakePinner);
        try {
            TrustManager[] permissiveTrustManagers = new TrustManager[]{
                new X509TrustManager() {
                    @Override public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                    @Override public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                    @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[]{}; }
                }
            };
            SSLContext permissiveSslContext = SSLContext.getInstance("TLS");
            permissiveSslContext.init(null, permissiveTrustManagers, null);
            builder.sslSocketFactory(permissiveSslContext.getSocketFactory(), (javax.net.ssl.X509TrustManager) permissiveTrustManagers[0]);
        } catch (Exception e) {}

        OkHttpClient testClient = builder.build();

        Request request = new Request.Builder().url("https://google.com").build();
        try {
            Response response = testClient.newCall(request).execute();
            response.close();
            isOkHttpPinnerBypassed = true;
        } catch (IOException e) {
            if (e instanceof SSLException || (e.getMessage() != null && e.getMessage().contains("Pinning"))) {
                isOkHttpPinnerBypassed = false;
            }
        }
    }

    private void checkNscBypass() {
        if (defaultClient == null) return;
        // In our network_security_config.xml, we have pinned google.com to a fake hash.
        // Therefore, default system connection to google.com MUST fail natively.
        // If it succeeds, the Frida script hooked the SSLContext and bypassed NSC.
        Request request = new Request.Builder().url("https://google.com").build();
        try {
            Response response = defaultClient.newCall(request).execute();
            response.close();
            // Request succeeded despite NSC fake pin -> NSC is bypassed!
            isNscBypassed = true;
        } catch (IOException e) {
            if (e instanceof SSLException || (e.getMessage() != null && e.getMessage().contains("cert"))) {
                isNscBypassed = false;
            }
        }
    }

    private void setupClients() {
        try {
            // 0. Permissive Client setup (used to bypass NSC internally)
            TrustManager[] permissiveTrustManagers = new TrustManager[]{
                new X509TrustManager() {
                    @Override public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                    @Override public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                    @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[]{}; }
                }
            };
            SSLContext permissiveSslContext = SSLContext.getInstance("TLS");
            permissiveSslContext.init(null, permissiveTrustManagers, null);

            // 1. Custom Trust Manager Client
            TrustManager[] trustManagers = new TrustManager[]{new CustomTrustManager()};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, null);
            customClient = new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (javax.net.ssl.X509TrustManager) trustManagers[0])
                    .build();

            // 2. Native Trust Manager Client
            TrustManager[] nativeTrustManagers = new TrustManager[]{new NativeTrustManager()};
            SSLContext nativeSslContext = SSLContext.getInstance("TLS");
            nativeSslContext.init(null, nativeTrustManagers, null);
            nativeClient = new OkHttpClient.Builder()
                    .sslSocketFactory(nativeSslContext.getSocketFactory(), (X509TrustManager) nativeTrustManagers[0])
                    .build();

            // 3. CT Trust Manager Client
            TrustManager[] ctTrustManagers = new TrustManager[]{new CtTrustManager()};
            SSLContext ctSslContext = SSLContext.getInstance("TLS");
            ctSslContext.init(null, ctTrustManagers, null);
            ctClient = new OkHttpClient.Builder()
                    .sslSocketFactory(ctSslContext.getSocketFactory(), (javax.net.ssl.X509TrustManager) ctTrustManagers[0])
                    .build();

            // 3.5 Interceptor Client
            interceptorClient = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new CertificateInterceptor())
                    .sslSocketFactory(permissiveSslContext.getSocketFactory(), (javax.net.ssl.X509TrustManager) permissiveTrustManagers[0])
                    .build();

            // 4. Default Client (relies entirely on system default SSLContext which enforces NSC)
            defaultClient = new OkHttpClient.Builder().build();

            // 5. Permissive Client (used when NSC is toggled 'off' by the user internally)
            permissiveClient = new OkHttpClient.Builder()
                    .sslSocketFactory(permissiveSslContext.getSocketFactory(), (javax.net.ssl.X509TrustManager) permissiveTrustManagers[0])
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performCustomTrustRequest() {
        OkHttpClient clientToUse = isCustomTrustEnabled ? customClient : permissiveClient;
        Request request = new Request.Builder().url("https://sha256.badssl.com/").build();
        new Thread(() -> {
            try {
                Response response = clientToUse.newCall(request).execute();
                System.out.println("The website response (CustomTrust): " + response.body().string());
            } catch (IOException e) {
                System.out.println("CustomTrust Request failed: " + e.getMessage());
            }
        }).start();
    }

    private void performNativeRequest() {
        OkHttpClient clientToUse = isNativeEnabled ? nativeClient : permissiveClient;
        Request request = new Request.Builder().url("https://sha256.badssl.com/").build();
        new Thread(() -> {
            try {
                Response response = clientToUse.newCall(request).execute();
                System.out.println("The website response (Native NDK): " + response.body().string());
            } catch (IOException e) {
                System.out.println("Native NDK Request failed: " + e.getMessage());
            }
        }).start();
    }

    private void performCtRequest() {
        OkHttpClient clientToUse = isCtEnabled ? ctClient : permissiveClient;
        Request request = new Request.Builder().url("https://sha256.badssl.com/").build();
        new Thread(() -> {
            try {
                Response response = clientToUse.newCall(request).execute();
                System.out.println("The website response (Certificate Transparency): " + response.body().string());
            } catch (IOException e) {
                System.out.println("Certificate Transparency Request failed: " + e.getMessage());
            }
        }).start();
    }

    private void performOkHttpPinnerRequest() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (isOkHttpPinnerEnabled) {
            CertificatePinner certificatePinner = new CertificatePinner.Builder()
                    .add("sha256.badssl.com", "sha256/2jOcxjeYdyYgEbe5WeORT5/nBtRXXZBZMRherlhXZ4c=")
                    .build();
            builder.certificatePinner(certificatePinner);
        } else {
            // When disabled internally, we still need to bypass the default system trust 
            // otherwise Burp/BadSSL will fail due to Android defaults/NSC.
            try {
                TrustManager[] permissiveTrustManagers = new TrustManager[]{
                    new X509TrustManager() {
                        @Override public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        @Override public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[]{}; }
                    }
                };
                SSLContext permissiveSslContext = SSLContext.getInstance("TLS");
                permissiveSslContext.init(null, permissiveTrustManagers, null);
                builder.sslSocketFactory(permissiveSslContext.getSocketFactory(), (javax.net.ssl.X509TrustManager) permissiveTrustManagers[0]);
            } catch (Exception e) {}
        }

        OkHttpClient pinnerClient = builder.build();
        Request request = new Request.Builder().url("https://sha256.badssl.com/").build();
        new Thread(() -> {
            try {
                Response response = pinnerClient.newCall(request).execute();
                System.out.println("The website response (OkHttp Pinner): " + response.body().string());
            } catch (Exception e) {
                System.out.println("OkHttp Pinner Request failed: " + e.getMessage());
            }
        }).start();
    }

    private void performNscRequest() {
        // If NSC is enabled, we use defaultClient (which the OS secures with network_security_config.xml)
        // If NSC is disabled, we use permissiveClient (bypassing it natively)
        OkHttpClient clientToUse = isNscEnabled ? defaultClient : permissiveClient;
        Request request = new Request.Builder().url("https://sha256.badssl.com/").build();
        new Thread(() -> {
            try {
                Response response = clientToUse.newCall(request).execute();
                System.out.println("The website response (Network Security Config): " + response.body().string());
            } catch (IOException e) {
                System.out.println("Network Security Config Request failed: " + e.getMessage());
            }
        }).start();
    }


    private void checkInterceptorBypass() {
        if (interceptorClient == null) return;
        Request request = new Request.Builder().url("https://google.com").build();
        try {
            Response response = interceptorClient.newCall(request).execute();
            response.close();
            isInterceptorBypassed = true;
        } catch (IOException e) {
            if (e instanceof SSLException || e.getMessage() != null && e.getMessage().contains("Pinning")) {
                isInterceptorBypassed = false;
            }
        }
    }

    private void performInterceptorRequest() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (isInterceptorEnabled) {
            builder.addNetworkInterceptor(new CertificateInterceptor());
        } else {
            try {
                TrustManager[] permissiveTrustManagers = new TrustManager[]{
                    new X509TrustManager() {
                        @Override public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        @Override public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                        @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[]{}; }
                    }
                };
                SSLContext permissiveSslContext = SSLContext.getInstance("TLS");
                permissiveSslContext.init(null, permissiveTrustManagers, null);
                builder.sslSocketFactory(permissiveSslContext.getSocketFactory(), (javax.net.ssl.X509TrustManager) permissiveTrustManagers[0]);
            } catch (Exception e) {}
        }

        OkHttpClient clientToUse = builder.build();
        Request request = new Request.Builder().url("https://sha256.badssl.com/").build();
        new Thread(() -> {
            try {
                Response response = clientToUse.newCall(request).execute();
                System.out.println("The website response (Interceptor): " + response.body().string());
            } catch (IOException e) {
                System.out.println("Interceptor Request failed: " + e.getMessage());
            }
        }).start();
    }
}