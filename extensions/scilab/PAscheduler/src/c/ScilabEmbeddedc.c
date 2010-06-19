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
jclass resultsAndLogsClass = NULL;
jmethodID hadScilabErrorId = NULL;
jmethodID getLogsId = NULL;
jmethodID getExceptionId = NULL;
jmethodID getResultAsStringId = NULL;
jclass throwable_class = NULL;
jclass stackElement_class = NULL;
jmethodID getStackTraceid = NULL;
jmethodID SEtoStringId = NULL;
jmethodID getMessageid = NULL;
jmethodID getCauseid = NULL;



int debugVal = 0;

int checkException();

void printExceptionObject(jthrowable exc, int *debug);

void printExceptionThrown(jthrowable exc);

void printLogs(jstring logs,int *debug);

void getStackTraceCause(char **causeMessage, char ***charElementMessage, int *stackLength, jthrowable exc);

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
	    sciprint("[ScilabEmbeddedc] Couldn't Find ScilabSolver Class\n");
	    *err = 2;
	    return;
    }

    createConnectionId = (*env)->GetStaticMethodID(env,scilabSolverClass, "createConnection", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    if (createConnectionId == 0) {
        sciprint("[ScilabEmbeddedc] Couldn't get createConnection method\n");
	    *err = 2;
	    return;
    }

    //solveId = (*env)->GetStaticMethodID(env,scilabSolverClass, "solve", "([Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II)[[Ljava/lang/String;");
    solveId = (*env)->GetStaticMethodID(env,scilabSolverClass, "solve", "([Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;II)[Lorg/ow2/proactive/scheduler/ext/scilab/embedded/ResultsAndLogs;");
    if (solveId == 0) {
        sciprint("[ScilabEmbeddedc] Couldn't get solve method\n");
        *err = 2;
        return;
    }

    resultsAndLogsClass = (*env)->FindClass(env, "Lorg/ow2/proactive/scheduler/ext/scilab/embedded/ResultsAndLogs;");
    if (resultsAndLogsClass == 0) {
	    sciprint("[ScilabEmbeddedc] Couldn't Find ResultsAndLogs Class\n");
	    *err = 2;
	    return;
    }

    hadScilabErrorId = (*env)->GetMethodID(env,resultsAndLogsClass, "hadScilabError", "()Z");
    if (hadScilabErrorId == 0) {
        sciprint("[ScilabEmbeddedc] Couldn't get hadScilabError method\n");
        *err = 2;
        return;
    }

    getLogsId = (*env)->GetMethodID(env,resultsAndLogsClass, "getLogs", "()Ljava/lang/String;");
    if (getLogsId == 0) {
        sciprint("[ScilabEmbeddedc] Couldn't get getLogs method\n");
        *err = 2;
        return;
    }

    getExceptionId = (*env)->GetMethodID(env,resultsAndLogsClass, "getException", "()Ljava/lang/Throwable;");
    if (getExceptionId == 0) {
        sciprint("[ScilabEmbeddedc] Couldn't get getException method\n");
        *err = 2;
        return;
    }

    getResultAsStringId = (*env)->GetMethodID(env,resultsAndLogsClass, "getResultAsString", "()Ljava/lang/String;");
    if (getResultAsStringId == 0) {
        sciprint("[ScilabEmbeddedc] Couldn't get getResultAsString method\n");
        *err = 2;
        return;
    }

    throwable_class = (*env)->FindClass (env, "java/lang/Throwable");
    if (throwable_class == 0) {
        sciprint("[ScilabEmbeddedc] Couldn't get Throwable class\n");
        *err = 2;
        return;
    }

    getCauseid = (*env)->GetMethodID(env, throwable_class, "getCause", "()Ljava/lang/Throwable;");
    if (getCauseid == 0) {        
        sciprint("[ScilabEmbeddedc] Couldn't get method\n");
        *err = 2;
        return;
    }

    stackElement_class = (*env)->FindClass (env, "java/lang/StackTraceElement");
    if (stackElement_class == 0) {
        sciprint("[ScilabEmbeddedc] Couldn't get StackTraceElement class\n");
        *err = 2;
        return;
    }

    getStackTraceid = (*env)->GetMethodID(env, throwable_class, "getStackTrace", "()[Ljava/lang/StackTraceElement;");
    if (getStackTraceid == 0) {
        sciprint("[ScilabEmbeddedc] Couldn't get getStackTrace method\n");
        *err = 2;
        return;
    }

    SEtoStringId = (*env)->GetMethodID(env, stackElement_class, "toString", "()Ljava/lang/String;");
    if (SEtoStringId == 0) {
            sciprint("[ScilabEmbeddedc] Couldn't get StackTraceElement.toString() method\n");
            *err = 2;
            return;
    }

    getMessageid = (*env)->GetMethodID(env, throwable_class, "getMessage", "()Ljava/lang/String;");
    if (getMessageid == 0) {
            sciprint("[ScilabEmbeddedc] Couldn't get getMessage method\n");
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
    int resultsAndLogsArrayLength;
    int nbFailure;
    int i;

    nbFailure = 0;

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
    resultsAndLogsArrayLength = (int) (*env)->GetArrayLength(env, resultsAndLogsArray);
    if (resultsAndLogsArrayLength == 0) {
        sciprint("[ScilabEmbeddedc] No result received\n");
        *err = 1;
        return;
    }

    if (*debug) {
        printf("[ScilabEmbeddedc] Before allocating array of %d elements\n", resultsAndLogsArrayLength);
        sciprint("[ScilabEmbeddedc] Before allocating array of %d elements\n", resultsAndLogsArrayLength);
    }

    *results = (char **) malloc((unsigned) ((resultsAndLogsArrayLength+1)* sizeof(char *)));

    if (*results == 0) {
        sciprint("[ScilabEmbeddedc] Memory error\n");
        *err = 1;
        return;
    }
    
    (*results)[resultsAndLogsArrayLength] = NULL;

//    resultsArray = (jobjectArray) (*env)->GetObjectArrayElement(env, resultsAndLogsArray, (jsize) 0);
//    logsArray = (jobjectArray) (*env)->GetObjectArrayElement(env, resultsAndLogsArray, (jsize) 1);
//    errorArray = (jobjectArray) (*env)->GetObjectArrayElement(env, resultsAndLogsArray, (jsize) 2);
//    arrayLength = (int) (*env)->GetArrayLength(env, resultsArray);
//
//    if (arrayLength == 0) {
//        sciprint("[ScilabEmbeddedc] No result received\n");
//        *err = 1;
//        return;
//    }
//    if (*debug) {
//        printf("[ScilabEmbeddedc] Before allocating array of %d elements\n", arrayLength);
//        sciprint("[ScilabEmbeddedc] Before allocating array of %d elements\n", arrayLength);
//    }
//    *results = (char **) malloc((unsigned) ((arrayLength+1)* sizeof(char *)));
//    if (*results == 0) {
//        sciprint("[ScilabEmbeddedc] Memory error\n");
//        *err = 1;
//        return;
//    }
//    (*results)[arrayLength] = NULL;


    for (i = 0; i < resultsAndLogsArrayLength; i++) {        
        jobject resultsAndLogs = (jobject) (*env)->GetObjectArrayElement(env, resultsAndLogsArray, (jsize) i);
        if (resultsAndLogs != NULL) {
             jboolean hadScilabError = (jboolean) (*env)->CallBooleanMethod(env,resultsAndLogs, hadScilabErrorId);
             jthrowable exc = (jthrowable) (*env)->CallObjectMethod(env,resultsAndLogs, getExceptionId);
             jstring logs = (jstring) (*env)->CallObjectMethod(env,resultsAndLogs, getLogsId);
             jstring res =  (jstring) (*env)->CallObjectMethod(env,resultsAndLogs, getResultAsStringId);
             if (hadScilabError != JNI_FALSE) {
                if (*debug) {
                    printf("[ScilabEmbeddedc] Scilab error in task %d\n",i);
                    sciprint("[ScilabEmbeddedc] Scilab error in task %d\n",i);
                }
                    // Printing the task log on the scilab console
                 printLogs(logs, debug);

                 nbFailure++;

                 (*results)[i] = (char *) calloc (1,sizeof(char));
             }
             else if (exc != NULL) {
                 if (*debug) {
                    printf("[ScilabEmbeddedc] Java Exception in task %d\n",i);
                    sciprint("[ScilabEmbeddedc] Java Exception in task %d\n",i);
                 }
                 if (logs != NULL) {
                    printLogs(logs, debug);
                 }
                 printExceptionObject(exc, debug);
                 nbFailure++;

                 (*results)[i] = (char *) calloc (1,sizeof(char));
             }
             else {
                 if (*debug) {
                    printf("[ScilabEmbeddedc] Normal execution in task %d\n",i);
                    sciprint("[ScilabEmbeddedc] Normal execution in task %d\n",i);
                 }
                 printLogs(logs, debug);
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
             }

        }

    }

    if (nbFailure > 0) {
        *err = 1;
        return;
    }

//    for (i = 0; i < resultsAndLogsArrayLength; i++) {
//		jstring log, res , error;
//        res = (jstring) (*env)->GetObjectArrayElement(env, resultsArray, (jsize) i);
//        if (res != NULL) {
//            int resLength = (int) (*env)->GetStringUTFLength(env, res);
//            (*results)[i] = (char *) malloc ((resLength+1)*sizeof(char));
//            if ((*results)[i] == 0) {
//                sciprint("[ScilabEmbeddedc] Memory error\n");
//                *err = 1;
//                return;
//            }
//            if (*debug) {
//                printf("[ScilabEmbeddedc] Before strcpy\n");
//                sciprint("[ScilabEmbeddedc] Before strcpy\n");
//            }
//
//            strcpy((*results)[i], (const char*) (*env)->GetStringUTFChars(env, res, NULL));
//        }
//        if (*debug) {
//            printf("[ScilabEmbeddedc] Before log printing\n");
//            sciprint("[ScilabEmbeddedc] Before strcpy\n");
//        }
//
//        // Printing the task log on the scilab console
//        log = (jstring) (*env)->GetObjectArrayElement(env, logsArray, (jsize) i);
//        if (log != NULL) {
//            int logLength = (int) (*env)->GetStringUTFLength(env, log);
//            if (logLength > 0) {
//                const char* log_message  = (const char*) (*env)->GetStringUTFChars(env, log, NULL);
//                sciprint("%s\n",log_message);
//                if (*debug) {
//                    printf("%s\n",log_message);
//                }
//            }
//        }
//        if (*debug) {
//            printf("[ScilabEmbeddedc] Before error printing\n");
//            sciprint("[ScilabEmbeddedc] Before error printing\n");
//        }
//        // Printing the task error message on the scilab console
//        error = (jstring) (*env)->GetObjectArrayElement(env, errorArray, (jsize) i);
//        if (error != NULL) {
//            int errorLength = (int) (*env)->GetStringUTFLength(env, error);
//            if (errorLength > 0) {
//                const char* error_message = (const char*) (*env)->GetStringUTFChars(env, error, NULL);
//                sciprint("%s\n",error_message);
//                if (*debug) {
//                    printf("%s\n",error_message);
//                }
//                *err = 1;
//                return;
//            }
//        }
//    }
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
        printExceptionThrown(exc);
        return 1;
    }
    return 0;
}

void printLogs(jstring logs,int *debug) {
    int logLength = (int) (*env)->GetStringUTFLength(env, logs);
    if (logLength > 0) {
         const char* log_message  = (const char*) (*env)->GetStringUTFChars(env, logs, NULL);
         sciprint("%s\n",log_message);
         if (*debug) {
             printf("%s\n",log_message);
         }
     }
}

void getStackTraceCause(char **causeMessage, char ***charElementMessage, int *stackLength, jthrowable exc) {
    jobjectArray stackArray;
    jstring message;
    int messageLength;
    int i;    

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

void printExceptionObject(jthrowable exc, int *debug) {
     int i,j;
     char **causeMessage;
     // Stack
     char ***charElementMessage;
     int* stackLength;
     int stackDepth;
     jobject cause;

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

      for (i=0; i < stackDepth; i++) {
          if (*debug) {
                printf("%s\n",causeMessage[i]);
          }
          sciprint("%s\n",causeMessage[i]);
          for (j = 0; j < stackLength[i]; j++) {
             if (*debug) {
                printf("  %s\n",charElementMessage[i][j]);
             }
             sciprint("  %s\n",charElementMessage[i][j]);
          }
          if (i < stackDepth -1) {
            if (*debug) {
                printf("\nCaused by:\n");
            }
            sciprint("\nCaused by:\n");
          }
      }
}

void printExceptionThrown(jthrowable exc) {
     int i,j;
     char **causeMessage;
     // Stack
     char ***charElementMessage;
     int* stackLength;
     int stackDepth;
     jobject cause;
     

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
