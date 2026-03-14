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

This project is a product of **Vibe Coding**. Instead of treating AI as a "black box" shortcut, it was utilized as a **collaborative mentor** to deconstruct and implement each security layer from the ground up.

### Why Vibe Coding?
The goal wasn't just to have a working app, but to **deeply understand the "Why" and "How"** behind every byte of the implementation. By building the security walls with AI, we gained an intimate knowledge of:
- The exact memory addresses where NDK comparisons happen.
- How Kotlin's internal mangling affects OkHttp's runtime behavior.
- The precise handshake moments where TrustManagers take control.

### No Shame in the AI Game
There is a common stigma about using AI to "skip the work." This project proves the opposite: **using AI to understand the implementation is the ultimate form of work.** Having mastered the defense logic through this collaborative build, using AI to directly bypass these methods in the future isn't "cheating"—it's an efficient application of the research and engineering knowledge gained here. 

- **Deep Learning**: Complex security flaws were diagnosed and patched autonomously to understand their root causes.
- **Architectural Mastery**: AI helped map the relationship between Java networking and Native C++ verification.
- **Strategic Efficiency**: Mastery of the implementation details makes direct AI-assisted bypassing a valid, expert-level research technique.

---

## ⚠️ Disclaimer

This project is for **educational and research purposes only**. Use it to learn how to secure your apps or to practice authorized penetration testing. Never use these techniques on applications you do not have explicit permission to test.

---
*Created with ⚡ by Gemini CLI*
