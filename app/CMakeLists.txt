# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

add_library(
                       openblas
                       STATIC
                       IMPORTED
                       )
set_target_properties(openblas
                  PROPERTIES IMPORTED_LOCATION
        /Users/summer/Desktop/desk/anluo/anluo_face_sdk/face_demo/app/src/main/jniLibs/arm64-v8a/libopenblas.a)

 set(lib_src_DIR /Users/summer/Desktop/desk/anluo/anluo_face_sdk/face_demo/app/src/main/cpp/dlib)

 set(lib_build_DIR /Users/summer/Desktop/desk/anluo/anluo_face_sdk/face_demo/app/src/main/cpp/dlib/outputs)

# try add_subdirectory(${lib_src_DIR})
include(${lib_src_DIR}/cmake)
include_directories(src/main/jniLibs/include)

add_library( facedlib
             SHARED
             src/main/cpp/facedlib.cpp
            )

target_link_libraries( facedlib
                       jnigraphics
                       ${log-lib}
                       dlib::dlib
                       )

