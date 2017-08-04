#!/bin/bash

cd hijack
cd jni
ndk-build
cd ..
adb push libs/armeabi/hijack /data/local/tmp/
cd ..

cd instruments
cd base
cd jni
ndk-build
cd ..
cd ..

cd example
cd jni
ndk-build
cd ..
adb push libs/armeabi/libexample.so /data/local/tmp/


