//
//  notifications.c
//  webtools
//
//  Created by beynet on 25/05/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//
#include <unistd.h>
#include "notifications.h"
#include "callback.h"


/*
 * Class:     Fd
 * Method:    natClose
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_beynet_utils_io_Fd_natClose
(JNIEnv *env, jobject obj,jint fd) {
    return(close(fd));
}

/*
 * Class:     Fd
 * Method:    natFsync
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_beynet_utils_io_Fd_natFsync
(JNIEnv *env, jobject obj,jint fd) {
    return(fsync(fd));
}

JNIEXPORT jint JNICALL Java_org_beynet_utils_event_file_FileChangeHandler_natInit
(JNIEnv * env, jobject obj) {
    /* creating kqueue fd */
    
    return(1);
}


/**
 * add a directory to watch
 */
JNIEXPORT jint JNICALL Java_org_beynet_utils_event_file_FileChangeHandler_natAddDirectory
(JNIEnv * env, jobject obj, jint fd, jstring path) {
    const char *str;
    jint res = -1 ;
    str = (*env)->GetStringUTFChars(env, path, NULL);
    if (str == NULL) {
        return((jint)-1) ; /* OutOfMemoryError already thrown */
    }
    res=addWatchedDirectory(str);
    (*env)->ReleaseStringUTFChars(env, path, str);
    TRACE("watch id =%d",res);
    return(res);
}


JNIEXPORT jint JNICALL Java_org_beynet_utils_event_file_FileChangeHandler_natRemoveDirectory
(JNIEnv * env, jobject obj, jint fd, jint watched) {
    return(removeWatchedDirectory(watched));
}


JNIEXPORT void JNICALL Java_org_beynet_utils_event_file_FileChangeHandler_natSelect 
(JNIEnv * env, jobject obj, jint fd,jint maxSeq) {
    jclass cls = (*env)->GetObjectClass(env, obj);
    jmethodID mid = (*env)->GetMethodID(env, cls, "onEvent", "(IILjava/lang/String;)V");
    if (mid == NULL) {
        return; /* method not found */
    }
    setJNIContext(env, obj, mid);
    mainLoop();
}
