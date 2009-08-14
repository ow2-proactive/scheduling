#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "sciprint.h"
#include "machine.h"

#include "jni.h"

JavaVM *jvm;       /* denotes a Java VM */
JNIEnv *env;       /* pointer to native method interface */

jclass scilabSolverClass;
jmethodID solveId = NULL;
jmethodID createConnectionId = NULL;
jclass stringClass = NULL;



int debugVal = 0;

int checkException();

void printException(jthrowable exc);

void C2F(cinitEmbedded) (int *err)
{
    jsize nbVMs;
    if (SciJNI_GetCreatedJavaVMs(&jvm, (jsize) 1, &nbVMs) < 0) {
        *err = 1;
	sciprint("[ScilabEmbeddedc] Can't connect to existing JVM\n");
	return;
    }
    if ((*jvm)->AttachCurrentThread(jvm,(void **)&env, NULL) < 0) {
        *err = 1;
	sciprint("[ScilabEmbeddedc] Error attaching the JVM to the current Thread\n");
	return;
    }

    stringClass = (*env)->FindClass(env, "java/lang/String");

    scilabSolverClass = (*env)->FindClass(env, "Lorg/ow2/proactive/scheduler/ext/scilab/embedded/ScilabSolver;");
    if (scilabSolverClass == 0) {
        *err = 2;
	sciprint("[ScilabEmbeddedc] Couldn't Find ScilabSolver Class\n");
	return;
    }

    createConnectionId = (*env)->GetStaticMethodID(env,scilabSolverClass, "createConnection", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    if (createConnectionId == 0) {
        sciprint("[ScilabEmbeddedc] Couldn't get createConnection method\n");
	*err = 2;
	return;
    }

    solveId = (*env)->GetStaticMethodID(env,scilabSolverClass, "solve", "([Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II)[[Ljava/lang/String;");
    if (solveId == 0) {
        sciprint("[ScilabEmbeddedc] Couldn't get solve method\n");
        *err = 2;
        return;
    }
    *err = 0;

}



void C2F(cconnect) (url, login, passwd, u, l, p, err)
    int *err, *u, *l, *p;
    const char *url;
    const char *login;
    const char *passwd;
{
    jstring jurl = (*env)->NewStringUTF(env,url);
    jstring jlogin = (*env)->NewStringUTF(env,login);
    jstring jpasswd = (*env)->NewStringUTF(env,passwd);
    (*env)->CallStaticVoidMethod(env,scilabSolverClass, createConnectionId, jurl, jlogin, jpasswd);
    if (checkException()) {
        *err = 1;
        return;
    }

}

void C2F(csciSolve)(inputScripts, functionsDefinition, mainscript, selectScript, debug, results, n, t, m, s, err)
    int *n,*t, *m,*s,*debug, *err;
    char ***inputScripts;
    char ***results;
    const char *mainscript;
    const char *selectScript;
    const char *functionsDefinition;
{
		jstring jstr ;
		jobjectArray inputScriptsArray ;
		jstring currentMainScript;
		jstring functionsDefinitionJava;
		jstring selectionScript ;
		jobjectArray resultsArray;
		jobjectArray resultsAndLogsArray;
		jobjectArray logsArray;
		jobjectArray errorArray;

		int arrayLength;
    int i;
    if (*n <= 0) {
        sciprint("[ScilabEmbeddedc] list of input scripts can't be empty (given list size : %d)\n", *n);
        *err = 1;
        return;
    }

    if (*debug) {
         printf("[ScilabEmbeddedc] sciSolve %d inputs, %d debug\n", *n, *debug);
         sciprint("[ScilabEmbeddedc] sciSolve %d inputs, %d debug\n", *n, *debug);
    }

    // Initialization of the Java string array
    jstr = (*env)->NewStringUTF(env,"");
    inputScriptsArray = (*env)->NewObjectArray(env, *n, stringClass, jstr);

    // Initialization of the main scripts
    currentMainScript = (*env)->NewStringUTF(env,mainscript);
    functionsDefinitionJava = (*env)->NewStringUTF(env,functionsDefinition);
    selectionScript = (*env)->NewStringUTF(env,selectScript);
    debugVal = *debug;

    // Initialization of the input scripts
    for (i = 0; i < *n; i++) {
        jstring jstr = (*env)->NewStringUTF(env, (*inputScripts)[i]);
        (*env)->SetObjectArrayElement(env, inputScriptsArray, i, jstr);
    }

    resultsAndLogsArray = (jobjectArray) (*env)->CallStaticObjectMethod(env,scilabSolverClass, solveId, inputScriptsArray, functionsDefinitionJava, currentMainScript,selectionScript, 3, debugVal);
    if (checkException()) {
       sciprint("[ScilabEmbeddedc] Exception occured in the job execution\n");
        *err = 1;
        return;
    }
    if ((*env)->GetArrayLength(env, resultsAndLogsArray) == 0) {
        sciprint("[ScilabEmbeddedc] No result received\n");
        *err = 1;
        return;
    }
    resultsArray = (jobjectArray) (*env)->GetObjectArrayElement(env, resultsAndLogsArray, (jsize) 0);
    logsArray = (jobjectArray) (*env)->GetObjectArrayElement(env, resultsAndLogsArray, (jsize) 1);
    errorArray = (jobjectArray) (*env)->GetObjectArrayElement(env, resultsAndLogsArray, (jsize) 2);
    arrayLength = (int) (*env)->GetArrayLength(env, resultsArray);

    if (arrayLength == 0) {
        sciprint("[ScilabEmbeddedc] No result received\n");
        *err = 1;
        return;
    }
    if (*debug) {
        printf("[ScilabEmbeddedc] Before allocating array of %d elements\n", arrayLength);
        sciprint("[ScilabEmbeddedc] Before allocating array of %d elements\n", arrayLength);
    }
    *results = (char **) malloc((unsigned) ((arrayLength+1)* sizeof(char *)));
    if (*results == 0) {
        sciprint("[ScilabEmbeddedc] Memory error\n");
        *err = 1;
        return;
    }
    (*results)[arrayLength] = NULL;
    for (i = 0; i < arrayLength; i++) {
		jstring log, res , error;
        res = (jstring) (*env)->GetObjectArrayElement(env, resultsArray, (jsize) i);
        if (res != NULL) {
            int resLength = (int) (*env)->GetStringUTFLength(env, res);
            (*results)[i] = (char *) malloc ((resLength+1)*sizeof(char));
            if ((*results)[i] == 0) {
                sciprint("[ScilabEmbeddedc] Memory error\n");
                *err = 1;
                return;
            }
            if (*debug) {
                printf("[ScilabEmbeddedc] Before strcpy\n");
                sciprint("[ScilabEmbeddedc] Before strcpy\n");
            }

            strcpy((*results)[i], (const char*) (*env)->GetStringUTFChars(env, res, NULL));
        }
        if (*debug) {
            printf("[ScilabEmbeddedc] Before log printing\n");
            sciprint("[ScilabEmbeddedc] Before strcpy\n");
        }

        // Printing the task log on the scilab console 
        log = (jstring) (*env)->GetObjectArrayElement(env, logsArray, (jsize) i);
        if (log != NULL) {
            int logLength = (int) (*env)->GetStringUTFLength(env, log);
            if (logLength > 0) {
                const char* log_message  = (const char*) (*env)->GetStringUTFChars(env, log, NULL);
                sciprint("%s\n",log_message);
                if (*debug) {
                    printf("%s\n",log_message);
                }
            }
        }
        if (*debug) {
            printf("[ScilabEmbeddedc] Before error printing\n");
            sciprint("[ScilabEmbeddedc] Before error printing\n");
        }
        // Printing the task error message on the scilab console
        error = (jstring) (*env)->GetObjectArrayElement(env, errorArray, (jsize) i);
        if (error != NULL) {
            int errorLength = (int) (*env)->GetStringUTFLength(env, error);
            if (errorLength > 0) {
                const char* error_message = (const char*) (*env)->GetStringUTFChars(env, error, NULL);
                sciprint("%s\n",error_message);
                if (*debug) {
                    printf("%s\n",error_message);
                }
                *err = 1;
                return;
            }
        }
    }
    if (*debug) {
        printf("[ScilabEmbeddedc] Normal termination\n");
        sciprint("[ScilabEmbeddedc] Normal termination\n");
    }

    *err = 0;

}

int checkException()
{
    jthrowable exc;
    exc = (*env)->ExceptionOccurred(env);
    if (exc) {
        printException(exc);
        return 1;
    }
    return 0;
}

void getStackTraceCause(char **causeMessage, char ***charElementMessage, int *stackLength, jthrowable exc) {
    jobjectArray stackArray;
    jstring message;
    int messageLength;
    int i;
    jclass throwable_class = (*env)->FindClass (env, "java/lang/Throwable");
    jclass stackElement_class = (*env)->FindClass (env, "java/lang/StackTraceElement");
    jmethodID getStackTraceid = (*env)->GetMethodID(env, throwable_class, "getStackTrace", "()[Ljava/lang/StackTraceElement;");
    jmethodID SEtoStringId = (*env)->GetMethodID(env, stackElement_class, "toString", "()Ljava/lang/String;");
    jmethodID getMessageid = (*env)->GetMethodID(env, throwable_class, "getMessage", "()Ljava/lang/String;");

    if (getMessageid == 0 || SEtoStringId == 0 || getStackTraceid == 0) {
        (*env)->ExceptionClear(env);
        sciprint("[ScilabEmbeddedc] Couldn't get method\n");
        return;
    }

    stackArray = (jobjectArray) (*env)->CallObjectMethod(env, exc, getStackTraceid);
    message = (jstring) (*env)->CallObjectMethod(env, exc, getMessageid);
    if (message != NULL) {
        messageLength = (*env)->GetStringUTFLength(env, message);
        *causeMessage = (char *)malloc(sizeof(char)*(messageLength+1));
        strcpy(*causeMessage,(*env)->GetStringUTFChars(env,message, NULL));
    }
    else {
        *causeMessage = (char *)malloc(sizeof(char));
        strcpy(*causeMessage,"");
    }
    *stackLength = (*env)->GetArrayLength(env, stackArray);
    *charElementMessage = (char **)malloc((*stackLength)*sizeof(char *));
    for (i = 0; i < *stackLength; i++) {
         jobject stackElement;
         jstring elementMessage;
         int elementMessageLength;

         stackElement = (jobject) (*env)->GetObjectArrayElement(env, stackArray, (jsize) i);
         elementMessage = (jstring) (*env)->CallObjectMethod(env, stackElement, SEtoStringId);
         elementMessageLength = (*env)->GetStringUTFLength(env, elementMessage);
         (*charElementMessage)[i] = (char *)malloc(sizeof(char)*(elementMessageLength+1));
         strcpy((*charElementMessage)[i],(*env)->GetStringUTFChars(env,elementMessage, NULL));

    }
}

void printException(jthrowable exc) {
     int i,j;
     char **causeMessage;
     // Stack
     char ***charElementMessage;
     int* stackLength;
     int stackDepth;
     jobject cause;


     jclass throwable_class = (*env)->FindClass (env, "java/lang/Throwable");
     jmethodID getCauseid = (*env)->GetMethodID(env, throwable_class, "getCause", "()Ljava/lang/Throwable;");
     if (getCauseid == 0) {
        (*env)->ExceptionClear(env);
        sciprint("[ScilabEmbeddedc] Couldn't get method\n");
        return;
      }

      // Finding stack depth
      cause = (*env)->CallObjectMethod(env, exc, getCauseid);
      stackDepth = 1;
      while (cause != NULL) {
          stackDepth++;
          cause = (*env)->CallObjectMethod(env, cause, getCauseid);
      }
      causeMessage = (char **) malloc(sizeof(char *)*stackDepth);
      charElementMessage = (char ***) malloc(sizeof(char **)*stackDepth);
      stackLength = (int *) malloc(sizeof(int)*stackDepth);

      cause = exc;

      for (i=0; i < stackDepth; i++) {
           getStackTraceCause(causeMessage+i,charElementMessage+i,stackLength+i,cause);
           cause = (*env)->CallObjectMethod(env, cause, getCauseid);
      }

     (*env)->ExceptionDescribe(env);
     (*env)->ExceptionClear(env);


      for (i=0; i < stackDepth; i++) {
          sciprint("%s\n",causeMessage[i]);
          for (j = 0; j < stackLength[i]; j++) {
            sciprint("  %s\n",charElementMessage[i][j]);
          }
          if (i < stackDepth -1) {
            sciprint("\nCaused by:\n");
          }
      }
}
