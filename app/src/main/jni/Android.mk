LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
 


LOCAL_MODULE    := libsepronik
LOCAL_SRC_FILES := mains.cpp


include $(BUILD_SHARED_LIBRARY)