#include "jni.h"
#include "testsuite_timer_micro_MicroTimer.h"
#include <sys/time.h>
#include <stdio.h>
JNIEXPORT jlongArray JNICALL Java_testsuite_timer_micro_MicroTimer_currentTime  (JNIEnv* env, jobject obj)
{
    struct timeval current; 
    struct timezone currentTZ;
    jsize size=2;
  
    jlongArray tablo = env->NewLongArray(size);
    jlong* localArray = env->GetLongArrayElements(tablo, NULL);
   
    gettimeofday(&current, &currentTZ); 
    localArray[0]=current.tv_sec;
    localArray[1]=current.tv_usec;
    // printf("Puting \n");
    env->ReleaseLongArrayElements(tablo, localArray, 0);
    return tablo;
}
