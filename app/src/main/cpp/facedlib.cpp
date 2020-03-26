#include <jni.h>
#include <string>
#include <time.h>
#include <stdlib.h>
#include <stdio.h>
#include <dlib/dnn.h>
#include <dlib/clustering.h>
#include <dlib/string.h>
#include <dlib/image_io.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing/render_face_detections.h>
#include <dlib/image_processing.h>
#include <dlib/gui_widgets.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <iostream>

#include <fstream>
#include <sstream>
#include <cstdlib>
#include <dlib/compress_stream.h>
#include <dlib/base64.h>
#include <dlib/matrix.h>


#define LOG_TAG     "DLIB_FACE_NATIVE"
#define LOGE(...)   __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGD(...)   __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

#define DETECT_ARRAY_SIZE 4

#define RGBA_A(p) (((p)& 0xFF000000) >> 24)
#define RGBA_R(p) (((p)& 0x00FF0000) >> 16)
#define RGBA_G(p) (((p) & 0x0000FF00) >> 8)
#define RGBA_B(p)  ((p) & 0x000000FF)
#define LICENCE_DATE 2021

using namespace dlib;
using namespace std;
//
template<template<int, template<typename> class, int, typename> class block, int N,
        template<typename> class BN, typename SUBNET>
using residual = add_prev1<block<N, BN, 1, tag1<SUBNET>>>;
//
template<template<int, template<typename> class, int, typename> class block, int N,
        template<typename> class BN, typename SUBNET>
using residual_down = add_prev2<avg_pool<2, 2, 2, 2, skip1<tag2<block<N, BN, 2, tag1<SUBNET>>>>>>;
//
template<int N, template<typename> class BN, int stride, typename SUBNET>
using block  = BN<con<N, 3, 3, 1, 1, relu<BN<con<N, 3, 3, stride, stride, SUBNET>>>>>;
//
template<int N, typename SUBNET> using ares      = relu<residual<block, N, affine, SUBNET>>;
template<int N, typename SUBNET> using ares_down = relu<residual_down<block, N, affine, SUBNET>>;
//
template<typename SUBNET> using alevel0 = ares_down<256, SUBNET>;
template<typename SUBNET> using alevel1 = ares<256, ares<256, ares_down<256, SUBNET>>>;
template<typename SUBNET> using alevel2 = ares<128, ares<128, ares_down<128, SUBNET>>>;
template<typename SUBNET> using alevel3 = ares<64, ares<64, ares<64, ares_down<64, SUBNET>>>>;
template<typename SUBNET> using alevel4 = ares<32, ares<32, ares<32, SUBNET>>>;
//
using anet_type = loss_metric<fc_no_bias<128, avg_pool_everything<
        alevel0<
                alevel1<
                        alevel2<
                                alevel3<
                                        alevel4<
                                                max_pool<3, 3, 2, 2, relu<affine<con<32, 7, 7, 2, 2,
                                                        input_rgb_image_sized<150>
                                                >>>>>>>>>>>>;

typedef struct t_face_description {
    matrix<float, 0, 1> face_feature;
    int name;
} T_FACE_DESCRIPTION;

int load_faces(std::vector<T_FACE_DESCRIPTION> &face_desc_vec);

template<
        typename T
>
void init_bmp(
        image_view<T> &image,
        rgb_pixel rgbPixel,
        int row,
        int col
);


dlib::frontal_face_detector detector = get_frontal_face_detector();
shape_predictor sp;
anet_type net;
std::vector<T_FACE_DESCRIPTION> vector_face_model;

bool g_isInit = false;
float face_threshold_value = 0.4;

// "%Y-%m-%d %H:%M:%S" 年 月 日 时 分 秒
char *getTime() {
    time_t timep;
    time(&timep);
    char tmp[64];
    strftime(tmp, sizeof(tmp), "%Y", localtime(&timep));
    return tmp;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_anloq_utils_DlibFaceNativeMethodUtils_initThresholdValue(
        JNIEnv *env, jobject /* this */,
        jfloat jint1) {
    face_threshold_value = jint1;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_anloq_utils_DlibFaceNativeMethodUtils_RecoFromRect(
        JNIEnv *env,
        jobject obj, jobject bitmap, jint left, jint top, jint right, jint bottom) {

    int l = left;
    int t = top;
    int r = right;
    int b = bottom;


    if (!g_isInit) {
        char *stime = getTime();
        int itime = atoi(stime);
        if (itime >= LICENCE_DATE) {
            LOGE("License has out of date.");
            return;
        }
        // 这两个模型跟detector没关系
        deserialize("/storage/emulated/0/face/a.dat") >> sp;
        deserialize("/storage/emulated/0/face/b.dat") >> net;
        g_isInit = true;
    }
    AndroidBitmapInfo bitmapInfo;
    int ret;
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &bitmapInfo)) < 0) {
        LOGE("get bimap info failed,error=%d", ret);
        return;
    }
    if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888!");
        return;
    }
    void *bitmapPixels;
    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed! error=%d", ret);
        return;
    }

    dlib::rectangle rect = rectangle(l, t, r, b);


    int row = bitmapInfo.height;
    int columns = bitmapInfo.width;

    matrix<rgb_pixel> matrix_;
    matrix_.set_size(row, columns);

    int ic = 0, ir = 0;

    int a, red, green, blue = 0;
    for (ir = 0; ir < row; ++ir) {
        for (ic = 0; ic < columns; ++ic) {
            void *pixel = NULL;
            pixel = ((uint32_t *) bitmapPixels) + ir * columns + ic;
            uint32_t v = *(uint32_t *) pixel;
            blue = RGBA_R(v);
            green = RGBA_G(v);
            red = RGBA_B(v);
            rgb_pixel rgb_pixel_ = rgb_pixel((unsigned char) red, (unsigned char) green,
                                             (unsigned char) blue);
            matrix_(ir, ic) = rgb_pixel_;

        }
    }

    auto shape = sp(matrix_, rect);
    matrix<rgb_pixel> face_chip;
    extract_image_chip(matrix_, get_face_chip_details(shape, 150, 0.25), face_chip);


    std::vector<matrix<rgb_pixel>> face_chip_vec;
    std::vector<matrix<float, 0, 1>> face_all;

    face_chip_vec.push_back(move(face_chip));

    // clock_t s = clock();

    face_all = net(face_chip_vec);
    matrix<float, 0, 1> dnn_128 = face_all[0];

    // clock_t e3 = clock();
    // LOGE("net %f seconds\n", (double)(e3 - s));

    AndroidBitmap_unlockPixels(env, bitmap);
    // 人脸识别
    for (T_FACE_DESCRIPTION T:vector_face_model) {
        matrix<float, 0, 1> dnn_ = T.face_feature;
        float offset = length(dnn_ - dnn_128);

        if (offset < face_threshold_value) {
            // env->FindClass 调java

            int name = T.name;
            LOGE("识别");
            // 调java，显示已开门,把name(userid)传上去
            jclass faceActivityCls = env->FindClass("com/anloq/utils/DlibFaceNativeMethodUtils");
            jmethodID openDoorFunction = env->GetMethodID(faceActivityCls, "openDoor", "(IFF)V");
            env->CallVoidMethod(obj, openDoorFunction, name, offset, face_threshold_value);
            env->DeleteLocalRef(faceActivityCls);
            // 回收在native层分配内存的bitmap
            jclass bitmapCls = env->GetObjectClass(bitmap);
            jmethodID recycleFunction = env->GetMethodID(bitmapCls, "recycle", "()V");
            if (recycleFunction == 0) {
                LOGE("error recycling!");
            }
            env->CallVoidMethod(bitmap, recycleFunction);
            jclass ref = env->FindClass("android/graphics/Bitmap");
            (env)->DeleteLocalRef(ref);
//            float offset = length(dnn_ - dnn_128);
            __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "name=%d,offset=%f", name, offset);
        } else {
            //float offset = length(dnn_ - dnn_128);
            int name = T.name;
            //__android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"name=%d,offset=%f",name,offset);
            // LOGE(length(dnn_ - dnn_128));
            LOGE("不认识啊");
        }
    }
    // 回收在native层分配内存的bitmap
    jclass bitmapCls = env->GetObjectClass(bitmap);
    jmethodID recycleFunction = env->GetMethodID(bitmapCls, "recycle", "()V");
    if (recycleFunction == 0) {
        LOGE("error recycling!");
    }
    env->CallVoidMethod(bitmap, recycleFunction);
    jclass ref = env->FindClass("android/graphics/Bitmap");
    (env)->DeleteLocalRef(ref);

}


extern "C"
JNIEXPORT void JNICALL
Java_com_anloq_utils_DlibFaceNativeMethodUtils_loadFaceFromRect(
        JNIEnv *env,
        jobject /* this */, jobject bitmap, jint userid, jint left, jint top, jint right,
        jint bottom) {

    vector_face_model.clear();

    // 初始化dlib的相关功能
    if (!g_isInit) {
        char *stime = getTime();
        int itime = atoi(stime);
        if (itime >= LICENCE_DATE) {
            LOGE("License has out of date.");
            return;
        }
        deserialize("/storage/emulated/0/face/a.dat") >> sp;
        deserialize("/storage/emulated/0/face/b.dat") >> net;
        g_isInit = true;
    }

    int l = left;
    int t = top;
    int r = right;
    int b = bottom;

    AndroidBitmapInfo bitmapInfo;
    int ret;
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &bitmapInfo)) < 0) {
        LOGE("get bimap info failed,error=%d", ret);
    }
    if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888!");
    }
    void *bitmapPixels;
    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed! error=%d", ret);
    }

    int row = bitmapInfo.height;
    int columns = bitmapInfo.width;

    matrix<rgb_pixel> matrix_;
    matrix_.set_size(row, columns);

    int ic = 0, ir = 0;

    int a, red, green, blue = 0;
    for (ir = 0; ir < row; ++ir) {
        for (ic = 0; ic < columns; ++ic) {
            void *pixel = NULL;
            pixel = ((uint32_t *) bitmapPixels) + ir * columns + ic;
            uint32_t v = *(uint32_t *) pixel;
            blue = RGBA_R(v);
            green = RGBA_G(v);
            red = RGBA_B(v);
            rgb_pixel rgb_pixel_ = rgb_pixel((unsigned char) red, (unsigned char) green,
                                             (unsigned char) blue);
            matrix_(ir, ic) = rgb_pixel_;

        }
    }

    AndroidBitmap_unlockPixels(env, bitmap);
    // 回收在native层分配内存的bitmap
    jclass bitmapCls = env->GetObjectClass(bitmap);
    jmethodID recycleFunction = env->GetMethodID(bitmapCls, "recycle", "()V");
    if (recycleFunction == 0) {
        LOGE("error recycling!");
    }
    env->CallVoidMethod(bitmap, recycleFunction);
    jclass ref = env->FindClass("android/graphics/Bitmap");
    (env)->DeleteLocalRef(ref);

    dlib::rectangle rect = rectangle(l, t, r, b);
    T_FACE_DESCRIPTION t_face_description1;
    auto shape = sp(matrix_, rect);
    matrix<rgb_pixel> face_chip;
    extract_image_chip(matrix_, get_face_chip_details(shape, 150, 0.25), face_chip);

    std::vector<matrix<rgb_pixel>> face_chip_vec;
    std::vector<matrix<float, 0, 1>> face_all;

    face_chip_vec.push_back(move(face_chip));

    face_all = net(face_chip_vec);
    matrix<float, 0, 1> dnn_128 = face_all[0];

    t_face_description1.face_feature = dnn_128;
    t_face_description1.name = userid;
    // 人脸底库下发
    vector_face_model.push_back(t_face_description1);
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_anloq_activity_AdActivity1_stringFromJNIDLIB(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "onFrame c++";
    return env->NewStringUTF(hello.c_str());
}


