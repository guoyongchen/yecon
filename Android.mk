#
# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
ifeq ($(BOARD_USES_YECON_NEWUI),true)
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)


$(shell date "+%Y-%m-%d %H:%M:%S" > $(LOCAL_PATH)/res/raw/date)
LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := android-common android-support-v13 photoview imageloader kwmusic android-support-v4

LOCAL_SRC_FILES := $(call all-java-files-under, src) $(call all-renderscript-files-under, src)
LOCAL_JAVA_LIBRARIES := autochips yecon
#LOCAL_SDK_VERSION := current

LOCAL_PACKAGE_NAME := CaroceanLauncher
#LOCAL_CERTIFICATE := shared
LOCAL_CERTIFICATE := platform
LOCAL_OVERRIDES_PACKAGES := Home

LOCAL_SHARED_LIBRARIES  := libExSPort
LOCAL_JNI_SHARED_LIBRARIES := libExSPort
LOCAL_REQUIRED_MODULES := libExSPort

include $(BUILD_PACKAGE)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += photoview:libs/photoview.jar \
	imageloader:libs/imageloader.jar \
	kwmusic:libs/kwmusic-autosdk-v1.9.9.jar
include $(BUILD_MULTI_PREBUILT)
include $(call all-makefiles-under,$(LOCAL_PATH))

endif
