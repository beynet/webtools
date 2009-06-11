#include <stdlib.h>
#include <errno.h>


#include <jni.h>
#include <stdio.h>

 
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/ioctl.h>

#include "nativ.h"

#ifdef LIBC_NO_INOTIFY
#include <sys/syscall.h>
#include <linux/inotify.h>
static int inotify_init (void)
{
	return syscall (__NR_inotify_init);
}
static int inotify_add_watch (int fd, const char *name, __u32 mask)
{
	return syscall (__NR_inotify_add_watch, fd, name, mask);
}

static int inotify_rm_watch (int fd, __u32 wd)
{
	return syscall (__NR_inotify_rm_watch, fd, wd);
}

#else
#include <sys/inotify.h>
#endif

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
  int _inotifyFd = inotify_init();
  return(_inotifyFd);
}


JNIEXPORT jint JNICALL Java_org_beynet_utils_event_file_FileChangeHandler_natAddDirectory
  (JNIEnv * env, jobject obj, jint fd, jstring path) {
  const jbyte *str;
  jint watch      ;
  
  str = (*env)->GetStringUTFChars(env, path, NULL);
  if (str == NULL) {
         return((jint)-1) ; /* OutOfMemoryError already thrown */
  }
  watch = inotify_add_watch(fd,str,IN_ALL_EVENTS);
  return(watch) ;
}

JNIEXPORT void JNICALL Java_org_beynet_utils_event_file_FileChangeHandler_natSelect 
(JNIEnv * env, jobject obj, jint fd,jint maxSeq) {
	  fd_set rfds       ;
	  int retval        ;
		struct timeval tv ;
    int _inotifyFd = fd;
    jclass cls = (*env)->GetObjectClass(env, obj);
    jmethodID mid = 
      (*env)->GetMethodID(env, cls, "onEvent", "(IILjava/lang/String;)V");
    if (mid == NULL) {
      return; /* method not found */
    }


		tv.tv_sec = maxSeq;
		tv.tv_usec = 0;

		FD_ZERO(&rfds);
		FD_SET(_inotifyFd, &rfds);

		retval = select(_inotifyFd+1, &rfds, NULL, NULL, &tv);
		if (retval>0) {
			int toRead,total,readed   ;
			struct inotify_event *evt ;
			unsigned char* buffer              ;
			if ( ioctl(_inotifyFd,FIONREAD,&toRead) == -1 ) {
				perror("ioctl");
				return;
			}
			
			// allocating buffer for event data
			// --------------------------------
      buffer=(unsigned char*)malloc(toRead);
      if (buffer==NULL) return;
			
			if ( (total=read(_inotifyFd,buffer,toRead)) == -1 ) {
				free(buffer);
				return;
			}
			readed = 0 ;
			while (total-readed!=0) {
        int             eventNumber              ;
				char*           associatedFileName = NULL;
				
				evt=(struct inotify_event*)(buffer+readed);
				readed+=sizeof(*evt)+evt->len;
				/* if it is a directory event we store associated file name */
        /* -------------------------------------------------------- */
				if (evt->len>0) {
					associatedFileName=evt->name;
				}
        eventNumber = evt->mask ;

        /* calling instance callback */
        /* ------------------------- */
        (*env)->CallVoidMethod(env, obj, mid,eventNumber,evt->wd,(*env)->NewStringUTF(env,associatedFileName));
			}
      free(buffer);
    }
    else if (retval==-1){
      if (errno==EINTR) {
        jclass newExcCls = (*env)->FindClass(env, 
                                      "java/lang/InterruptedException");
        if (newExcCls == NULL) {
          /* Unable to find the exception class, give up. */
          return;
        }
        (*env)->ThrowNew(env, newExcCls, "thrown from C code");
      }
    }
}
