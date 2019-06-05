LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_C_INCLUDES += $(JNI_H_INCLUDE) 
LOCAL_SRC_FILES := SerialPort.c
LOCAL_SHARED_LIBRARIES := liblog libcutils
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := libExSPort
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)