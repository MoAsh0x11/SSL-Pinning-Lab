# SSL Pinning Laboratory 🧪

A comprehensive Android laboratory designed to demonstrate, test, and bypass various SSL Pinning implementations. This project was built entirely through **Vibe Coding**—using AI-driven autonomous development to scaffold, debug, and refine security mechanisms in real-time.

## 🚀 Overview

This app provides a controlled environment to practice mobile security research. It implements six different layers of SSL Pinning, ranging from standard high-level configurations to low-level native NDK implementations.

### Features
- **Real-time Bypass Detection**: The app constantly monitors its own security state and updates the UI buttons (**GREEN** for secure, **RED** for bypassed).
- **Multiple Pinning Layers**:
    - **Custom Trust Manager**: Manual X.509 certificate validation.
    - **OkHttp CertificatePinner**: Industry-standard pinning.
    - **Network Security Config (NSC)**: Android's native XML-based pinning.
    - **Native NDK Pinning**: C++ (JNI) based public key verification.
    - **Certificate Transparency (CT)**: Validation of SCT extensions.
    - **OkHttp Interceptor**: Final-check validation within the network stack.

---

## 🛠 Project Structure

- `app/`: The main Android application module.
- `nativepinning/`: NDK module containing the C++ SHA-256 verification logic.
- `*.js`: A collection of specialized Frida scripts for researchers to test bypasses.

---

## ⚡ Frida Bypass Scripts

This repository includes several scripts to test the robustness of the implementations:

1. **`complete_bypass.js`**: The "Universal" bypass. It uses dynamic signature detection to hook all OkHttp (v3 & v4 Kotlin), Conscrypt, and Android NSC overloads.
2. **`bypass_ndk_only.js`**: A surgical bypass that hooks the compiled C++ memory inside `libnativepinning.so` without touching the Java layer.
3. **`bypass_trustmanagers.js`**: Targeted bypass for Java-based TrustManager implementations.

---

## 🎸 Built with Vibe Coding

This project is a product of **Vibe Coding**. Instead of manual boilerplate and traditional debugging, the entire architecture—from the NDK integration to the complex Frida bypass logic—was orchestrated through high-level intent and AI collaboration.

- **Fast Iteration**: Complex security flaws (like OkHttp chain cleaning interference) were diagnosed and patched autonomously.
- **AI-Native Security**: Implementation of both the security "walls" and the "ladders" (Frida scripts) to climb over them.
- **Surgical Logic**: Custom background threads for bypass detection were tuned to avoid false positives by isolating network layers.

---

## ⚠️ Disclaimer

This project is for **educational and research purposes only**. Use it to learn how to secure your apps or to practice authorized penetration testing. Never use these techniques on applications you do not have explicit permission to test.

---
*Created with ⚡ by Gemini CLI*
