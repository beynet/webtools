//
//  callback.h
//  webtools
//
//  Created by beynet on 25/05/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//
#include <CoreServices/CoreServices.h>
#include <JavaVM/jni.h>



#ifdef TRACING
#define TRACE(...) fprintf(stderr,__VA_ARGS__)
#else
#define TRACE(...)
#endif


/**
 * callback processing events on the level of a directory
 */
void mycallback(
               ConstFSEventStreamRef streamRef,
               void *clientCallBackInfo,
               size_t numEvents,
               void *eventPaths,
               const FSEventStreamEventFlags eventFlags[],
                const FSEventStreamEventId eventIds[]);

/**
 * add a directory to be watched, return id of the watch or -1 in case of error
 */
int32_t addWatchedDirectory(const char* directory) ;

/**
 * remove a watched directory
 */
jint removeWatchedDirectory(jint watched);

/**
 * main event loop - processing events
 */
void mainLoop() ;

/*
 *
 * Define the jni context to be able to call java object storing notification
 */
void setJNIContext(JNIEnv * envi, jobject obji,jmethodID midi) ;