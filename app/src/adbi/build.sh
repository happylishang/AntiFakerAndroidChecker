#!/bin/sh

cd hijack/jni
ndk-build
cd ../..

cd instruments
cd base/jni
ndk-build
cd ../..

cd example/jni
ndk-build
cd ../..

cd ..

