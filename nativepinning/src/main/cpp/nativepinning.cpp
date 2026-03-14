#include <jni.h>
#include <string.h>
#include <stdint.h>
#include "sha256.h"

#define SHA256_DIGEST_LENGTH 32

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_hacking_sslpinninglab_network_NativeVerifier_verifyPublicKey(
        JNIEnv *env,
        jobject thiz,
        jbyteArray publicKeyBytes) {

    if (publicKeyBytes == NULL) return JNI_FALSE;

    // Get raw bytes from Java array
    jbyte* key = env->GetByteArrayElements(publicKeyBytes, NULL);
    jsize len = env->GetArrayLength(publicKeyBytes);

    // Prepare buffer for the hash
    unsigned char hash[SHA256_DIGEST_LENGTH];

    // Compute SHA-256 natively
    SHA256_CTX ctx;
    sha256_init(&ctx);
    sha256_update(&ctx, (const uint8_t*) key, len);
    sha256_final(&ctx, hash);

    // Clean up JNI reference
    env->ReleaseByteArrayElements(publicKeyBytes, key, JNI_ABORT);

    // 3. Your Pinned Hash
    const uint8_t pinnedHash[SHA256_DIGEST_LENGTH] = {
            0xda, 0x33, 0x9c, 0xc6, 0x37, 0x98, 0x77, 0x26,
            0x20, 0x11, 0xb7, 0xb9, 0x59, 0xe3, 0x91, 0x4f,
            0x9f, 0xe7, 0x06, 0xd4, 0x57, 0x5d, 0x90, 0x59,
            0x31, 0x18, 0x5e, 0xae, 0x58, 0x57, 0x67, 0x87
    };

    // 4. Comparison
    if (memcmp(hash, pinnedHash, SHA256_DIGEST_LENGTH) == 0) {
        return JNI_TRUE;
    }

    return JNI_FALSE;
}