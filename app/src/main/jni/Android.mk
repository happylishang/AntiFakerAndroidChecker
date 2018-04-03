LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_LDLIBS:=-L$(SYSROOT)/usr/lib -llog
LOCAL_MODULE    := emulator_check
LOCAL_SRC_FILES := emulator/com.snail.device_jni_EmulatorDetectUtil.c
include $(BUILD_SHARED_LIBRARY)


LOCAL_PATH := $(call my-dir)
