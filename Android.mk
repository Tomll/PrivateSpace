LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional 
LOCAL_SRC_FILES := $(call all-java-files-under, src) 
LOCAL_PACKAGE_NAME := PrivateSpace
#LOCAL_JACK_ENABLED := disabled
#LOCAL_PROGUARD_ENABLED := disabled
LOCAL_PROGUARD_ENABLED := custom
#PRIVATE_PROGUARD_ENABLED := true
LOCAL_PROGUARD_FLAG_FILES := proguard.flags
#LOCAL_MODULE_CLASS := APPS
LOCAL_CERTIFICATE := platform
LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_JAVA_LIBRARIES += telephony-common
LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-v4 \
    android-support-v7-recyclerview \
    android-support-v7-appcompat \
    android-support-v7-cardview \
    android-support-design \
    commons-codec-1.4 \
    glide-3.7.0

LOCAL_RESOURCE_DIR = $(LOCAL_PATH)/res \
    frameworks/support/v7/recyclerview/res \
    frameworks/support/v7/appcompat/res \
    frameworks/support/v7/cardview/res \
    frameworks/support/design/res

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages android.support.v7.recyclerview \
    --extra-packages android.support.v7.appcompat \
    --extra-packages android.support.v7.cardview \
    --extra-packages android.support.design

#LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
###LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=supportv4:libs/android-support-v4.jar
###LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=supportv7appcompat:libs/android-support-v7-appcompat.jar
###LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=supportv7recyclerview:libs/android-support-v7-recyclerview.jar
###LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=supportdesign:libs/android-support-design.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=commons-codec-1.4:libs/commons-codec-1.4.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=glide-3.7.0:libs/glide-3.7.0.jar

include $(BUILD_MULTI_PREBUILT)
include $(call all-makefiles-under,$(LOCAL_PATH))

