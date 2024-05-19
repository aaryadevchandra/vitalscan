#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <android/log.h>

extern "C" JNIEXPORT jbyteArray JNICALL

Java_com_actofit_vitalscan_MainActivity_convertYUV420888ToNV21(
        JNIEnv* env,
        jobject /* this */,
        jint imageWidth,
        jint imageHeight,
        jobject yPlane,
        jobject uPlane,
        jobject vPlane,
        jint uPixelStride,
        jint vPixelStride,
        jint uRowStride,
        jint vRowStride) {

    auto* yBuf = static_cast<jbyte*>(env->GetDirectBufferAddress(yPlane));
    auto* uBuf = static_cast<jbyte*>(env->GetDirectBufferAddress(uPlane));
    auto* vBuf = static_cast<jbyte*>(env->GetDirectBufferAddress(vPlane));

    jint ySize = imageWidth * imageHeight;
    jint uvSize = ySize / 2;
    jbyteArray nv21 = env->NewByteArray(ySize + uvSize);

    if (nv21 == nullptr) {
        // Out of memory error.
        return nullptr;
    }

    env->SetByteArrayRegion(nv21, 0, ySize, yBuf);

    jbyte* nv21Buf = env->GetByteArrayElements(nv21, nullptr);

    jint pos = ySize;
    for (jint row = 0; row < imageHeight / 2; ++row) {
        for (jint col = 0; col < imageWidth / 2; ++col) {
            jint uIndex = col * uPixelStride + row * uRowStride;
            jint vIndex = col * vPixelStride + row * vRowStride;

            nv21Buf[pos++] = uBuf[uIndex];
            nv21Buf[pos++] = vBuf[vIndex];
        }
    }

    env->ReleaseByteArrayElements(nv21, nv21Buf, 0);
    return nv21;
}