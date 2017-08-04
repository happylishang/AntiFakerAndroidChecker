adbi - The Android Dynamic Binary Instrumentation Toolkit  
=========================================================

Simple binary instrumentation toolkit for Android ARM + Thumb.

Instrumentation is based on library injection and hooking function entry 
points (in-line hooking).

The toolkit consists of two main components the hijack tool and the base 
library.

**hijack**

 The hijack tool provides the injection functionality. It supports a number of  modes for supporting older and newer Android devices. hijack provides help on the command line.
 
**libbase**

 The base library provides the hooking and unhooking functionality. The base library is compiled as a static library so it can be directly included in the
 actual instrumentation library. This is done so we can keep everything in /data/local/tmp. 

Below we provide and easy to follow step-by-step instructions for howto build and use adbi. The example instrument hijacks epoll_wait() and logs every call
to a file.

=== External Resources ===

more information at: 
 http://www.mulliner.org/android/

slide deck about this toolkit: 
 http://www.mulliner.org/android/feed/binaryinstrumentationandroid_mulliner_summercon12.pdf
 
old code (with more examples):
 http://www.mulliner.org/android/feed/collin_android_dbi_v02.zip

=== Prerequisites ===

Android SDK
Android NDK

=== How to Build ===

= build the hijack tool =
```
cd hijack
cd jni
ndk-build
cd ..
adb push libs/armeabi/hijack /data/local/tmp/
cd ..
```

= build the instrumentation base code =

```
cd instruments
cd base
cd jni
ndk-build
cd ..
cd ..
```

= build instrumentation example =

```
cd example
cd jni
ndk-build
cd ..
adb push libs/armeabi/libexample.so /data/local/tmp/
```

=== How to Run ===

```
adb shell
su
cd /data/local/tmp
>/data/local/tmp/adbi_example.log
# GET PID from com.android.phone
./hijack -d -p PID -l /data/local/tmp/libexample.so
cat adbi_example.log
```

output should look similar to:

```
started
hooking:   epoll_wait = 0x4004c378 ARM using 0x4a84a588
epoll_wait() called
epoll_wait() called
epoll_wait() called
removing hook for epoll_wait()
```

