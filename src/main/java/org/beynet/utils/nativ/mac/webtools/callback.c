//
//  callback.c
//  webtools
//
//  Created by beynet on 25/05/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#include "callback.h"
#include <dirent.h>
#include <unistd.h>
#include <search.h>
#include <pthread.h>
#include <sys/stat.h>
#include <CoreServices/CoreServices.h>

typedef struct TreeFileInfo_t {
    char* name ;
    struct	timespec mtime ;
    struct timespec ctime ;
    
} TreeFileInfo ;

typedef struct WatchedDirectoryList_t {
    char*              dirName               ;
    void*              snapShotTree          ;
    TreeFileInfo**     snapShotList          ;
    int                snapShotElementCount  ;
    FSEventStreamRef               stream    ;
    int32_t                        offset    ;
    struct WatchedDirectoryList_t* next      ;
    struct WatchedDirectoryList_t* previous  ;
    struct WatchedDirectoryList_t* free      ;
} WatchedDirectoryList;


static const int IN_ACCESS		=	0x00000001;	/* File was accessed */
static const int IN_MODIFY		=	0x00000002;	/* File was modified */
static const int IN_ATTRIB		=	0x00000004;	/* Metadata changed */
static const int IN_CLOSE_WRITE	=	0x00000008;	/* Writtable file was closed */
static const int IN_CLOSE_NOWRITE=	0x00000010;	/* Unwrittable file closed */
static const int IN_OPEN			=	0x00000020;	/* File was opened */
static const int IN_MOVED_FROM	=	0x00000040;	/* File was moved from X */
static const int IN_MOVED_TO		=	0x00000080;	/* File was moved to Y */
static const int IN_CREATE		=	0x00000100;	/* Subfile was created */
static const int IN_DELETE		=	0x00000200;	/* Subfile was deleted */
static const int IN_DELETE_SELF	=	0x00000400;	/* Self was deleted */
static const int IN_MOVE_SELF	=	0x00000800;	/* Self was moved */

/* list of all the directories we are listening to */
static WatchedDirectoryList* directories = NULL;

/* mutext used to protect write operatorions on directories list */
static pthread_mutex_t watchedDirectoriesMutex = PTHREAD_MUTEX_INITIALIZER;

/* static used to store main CFRunLoopRef */
static CFRunLoopRef mainThreadRunLoopRef = NULL;

static JNIEnv * env;
static jobject obj;
static jmethodID mid;

/**
 * start the list
 */
static WatchedDirectoryList* _initiateWatchedDictectoriesList() {
    WatchedDirectoryList* result = malloc(sizeof(WatchedDirectoryList));
    directories = malloc(sizeof(WatchedDirectoryList));
    directories->previous=directories->next=directories->free=NULL;
    directories->offset = 0 ;
    result->previous = directories;
    result->next = NULL ;
    result->offset = 1 ;
    directories->next = result ;
    return(result);
}

/**
 insert at the end the newlly allocated block in the list ordered by offset
 */
static void _insertNewEmptyWatchedDirectory(WatchedDirectoryList* new) {
    WatchedDirectoryList* tmp = directories ;
    while(tmp->next!=NULL && tmp->next->offset<new->offset) tmp = tmp->next;
    if (tmp->next!=NULL) {
        tmp->next->previous=new;
        new->next=tmp->next;
    }
    else {
        new->next = NULL ;
    }
    tmp->next=new ;
    new->previous=tmp;
    if (new->offset==INT_MAX) new->offset=tmp->offset+1;
}

/**
 * 
 */
static WatchedDirectoryList* _getNewEmptyWatchedDirectory() {
    WatchedDirectoryList* result = NULL ;
    TRACE("Searching Free block\n");
    /* a free block is found */
    if (directories->free!=NULL) {
        TRACE("Free block found\n");
        result=directories->free;
        directories->free=result->next;
        if (directories->free!=NULL) directories->free->previous=NULL;
    }
    /* allocating new block */
    else {
        result = malloc(sizeof(WatchedDirectoryList));
        if (result==NULL) return(NULL);
        result->offset = INT_MAX;
    }
    _insertNewEmptyWatchedDirectory(result);
    return(result);
}

/**
 * create a new watchedDirectory object
 * return newly created object or NULL in case of allocation error
 */
static WatchedDirectoryList* createNewWatchedDirectory(const char* directory) {
    pthread_mutex_lock(&watchedDirectoriesMutex);
    WatchedDirectoryList* newDir = NULL ;
    if (directories==NULL) {
        newDir =_initiateWatchedDictectoriesList();
    }
    else {
        newDir = _getNewEmptyWatchedDirectory();
    }
    if (newDir!=NULL) newDir->dirName=strdup(directory);
    pthread_mutex_unlock(&watchedDirectoriesMutex);
    return(newDir);
}

static int mystrcmp(const void* ch1,const void*ch2) {
    TreeFileInfo* t1 = (TreeFileInfo*)ch1 ;
    TreeFileInfo* t2 = (TreeFileInfo*)ch2 ;
    return(strcmp(t1->name,t2->name));
}


char* createFileName(const char* dirName,char* fileName,int fileNameSize) {
    char *result = calloc(1,fileNameSize+1+strlen(dirName)+1);
    strcpy(result,dirName);
    result[strlen(dirName)]='/';
    strncat(result,fileName,fileNameSize);
    return(result);
}

/**
 * snapshot directory
 */
int snapshotDirectory(WatchedDirectoryList* directory) {
    int j = 0 ;
    directory->snapShotTree = NULL ;
    TRACE("taking snap\n");
    struct dirent **result ;
    directory->snapShotElementCount = scandir(directory->dirName, &result, NULL,alphasort);
    directory->snapShotList = calloc(directory->snapShotElementCount,sizeof(TreeFileInfo*));
    for (int i=0;i<directory->snapShotElementCount;i++){
        if (result[i]->d_type==DT_REG) {
            TreeFileInfo* newFileInfo = calloc(1,sizeof(TreeFileInfo));
            directory->snapShotList[j++]=newFileInfo;
            struct stat statFile ;
            memset(&statFile,0,sizeof(statFile));
            if (newFileInfo==NULL) return -1;
            newFileInfo->name = createFileName(directory->dirName,result[i]->d_name,result[i]->d_namlen);
            stat(newFileInfo->name,&statFile);
            newFileInfo->mtime.tv_sec=statFile.st_mtimespec.tv_sec;
            newFileInfo->mtime.tv_nsec=statFile.st_mtimespec.tv_nsec;
            newFileInfo->ctime=statFile.st_ctimespec;
            tsearch(newFileInfo,&(directory->snapShotTree),&mystrcmp);
        }
        free(result[i]);
    }
    free(result);
    directory->snapShotElementCount = j;
    TRACE("end of taking snap\n");
    return(0);
}

/**
 * create a new stream watching event on the fs
 */
int32_t addWatchedDirectory(const char* directory) {
    TRACE("Adding directory %s\n",directory);
    CFStringRef mypath = CFStringCreateWithCString(NULL,directory,kCFStringEncodingUTF8);
    CFArrayRef pathsToWatch = CFArrayCreate(NULL, (const void **)&mypath, 1, NULL);
    CFAbsoluteTime latency = 1.0; /* Latency in seconds */
    
    WatchedDirectoryList* newDir = createNewWatchedDirectory(directory);
    if (newDir==NULL) return(-1);
    FSEventStreamContext context ;
    context.info = newDir ;
    context.retain=NULL;
    context.release=NULL;
    context.copyDescription = NULL ;
    context.version = 0 ;
    /* Create the stream, passing in a callback */
    newDir->stream = FSEventStreamCreate(NULL,
                                         &mycallback,
                                         &context,
                                         pathsToWatch,
                                         kFSEventStreamEventIdSinceNow, /* Or a previous event ID */
                                         latency,
                                         kFSEventStreamCreateFlagNone /* Flags explained in reference */
                                         );
    TRACE("Adding stream to run loop\n");
    FSEventStreamScheduleWithRunLoop(newDir->stream, mainThreadRunLoopRef,         kCFRunLoopDefaultMode);
    FSEventStreamStart(newDir->stream);
    snapshotDirectory(newDir);
    return(newDir->offset);
}

void mainLoop() {
    TRACE("Entering main loop\n");
    if (mainThreadRunLoopRef==NULL) mainThreadRunLoopRef = CFRunLoopGetCurrent();
    SInt32    result = CFRunLoopRunInMode(kCFRunLoopDefaultMode, 1, true);
    if (result==kCFRunLoopRunFinished) sleep(1);
    TRACE("End of main loop\n");
}


void setJNIContext(JNIEnv * envi, jobject obji,jmethodID midi) {
    env=envi;
    obj=obji;
    mid=midi;
}

/**
 * free all memory taken by a snapshot
 */
void freeSnapShot(void* snapShotTree,TreeFileInfo** snapShotList,int size) {
    for (int i=0;i<size;i++) {
        tdelete(snapShotList[i], &snapShotTree, mystrcmp);
        free(snapShotList[i]->name);
        free(snapShotList[i]);
    }
    
    free(snapShotList);
}

/**
 * compare two versions of a directory snapshor
 */
void compareSnapShots(WatchedDirectoryList* current) {
    void* previousSnapShotTree = current->snapShotTree;
    TreeFileInfo** previousSnapShotList = current->snapShotList;
    int previousSize = current->snapShotElementCount;
    snapshotDirectory(current);
    
    // walking into previous snap to detect file removed
    // -------------------------------------------------
    for (int i=0;i<previousSize;i++) {
        TreeFileInfo* old = previousSnapShotList[i];
        TreeFileInfo** pfound = tfind(old,&(current->snapShotTree),&mystrcmp);
        if (pfound==NULL) {
            TRACE("File %s has been removed\n",old->name);
            /* calling instance callback */
            /* ------------------------- */
            (*env)->CallVoidMethod(env, obj, mid,0x00000200,current->offset,(*env)->NewStringUTF(env,old->name));
        }
        else {
            TreeFileInfo* found = *pfound;
            if (found->mtime.tv_sec!=old->mtime.tv_sec || found->mtime.tv_nsec!=old->mtime.tv_nsec) {
                TRACE("File %s has been modified\n",old->name);
                /* calling instance callback */
                /* ------------------------- */
                (*env)->CallVoidMethod(env, obj, mid,0x00000002,current->offset,(*env)->NewStringUTF(env,old->name));
            }
        }
    }
    TRACE("scanning new snap\n");
    // walking into new snap to detect new files
    // -----------------------------------------
    for (int i=0;i<current->snapShotElementCount;i++) {
        TreeFileInfo* new = current->snapShotList[i];
        TreeFileInfo** pfound = tfind(new,&(previousSnapShotTree),&mystrcmp);
        if (pfound==NULL) {
            TRACE("File %s has been added\n",new->name);
            /* calling instance callback */
            /* ------------------------- */
            (*env)->CallVoidMethod(env, obj, mid,0x00000100,current->offset,(*env)->NewStringUTF(env,new->name));
        }
    }
    
    // free memory taken by previous snapshot
    // --------------------------------------
    freeSnapShot(previousSnapShotTree, previousSnapShotList, previousSize);
}



/**
 *
 * remove a watched directory
 *
 */
int removeWatchedDirectory(jint watched){
    int status = -1; 
    pthread_mutex_lock(&watchedDirectoriesMutex);
    WatchedDirectoryList* current = directories ;
    while (current!=NULL && current->offset!=watched) current=current->next;
    if (current!=NULL) {
        TRACE("Removing dir %s\n",current->dirName);
        if(current->next!=NULL) current->next->previous=current->previous;
        current->previous->next=current->next;
        free(current->dirName);
        freeSnapShot(current->snapShotTree, current->snapShotList, current->snapShotElementCount);
        FSEventStreamStop(current->stream);
        FSEventStreamInvalidate(current->stream);
        FSEventStreamRelease(current->stream);
        TRACE("Stream released\n");
        current->previous=NULL;
        current->next=directories->free;
        directories->free=current;
        if (current->next!=NULL) current->next->previous=current;
        status=0;
    }
    pthread_mutex_unlock(&watchedDirectoriesMutex);
    TRACE("Block removed\n");
    return(status);
}

/**
 * call back called when an event on a directory is detected
 */
void mycallback(
                ConstFSEventStreamRef streamRef,
                void *clientCallBackInfo,
                size_t numEvents,
                void *eventPaths,
                const FSEventStreamEventFlags eventFlags[],
                const FSEventStreamEventId eventIds[]) {
    int i;
    TRACE("Entering callback\n");
    // printf("Callback called\n");
    for (i=0; i<numEvents; i++) {
        /* flags are unsigned long, IDs are uint64_t */
        WatchedDirectoryList* watched = (WatchedDirectoryList*) clientCallBackInfo;
        TRACE("Watched dir offset = %d\n",watched->offset);
        compareSnapShots(watched);
    }
}
