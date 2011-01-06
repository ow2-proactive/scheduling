#include "api_scilab.h"
#include "ScilabJavaObject2.hxx"

namespace ScilabObjects {

void ScilabJavaObject2::unwrapBoolean (JavaVM * jvm_, int id, int pos){
    
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jbooleanunwrapBooleanjintID = curEnv->GetStaticMethodID(cls, "unwrapBoolean", "(I)Z" ) ;
  if (jbooleanunwrapBooleanjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapBoolean");
    }

  int *addr;
  SciErr err = allocMatrixOfBoolean(pvApiCtx, pos, 1, 1, &addr);
  if (err.iErr)
    {
      throw "No more memory.";
    }

  *addr = static_cast<jboolean>(curEnv->CallStaticBooleanMethod(cls, jbooleanunwrapBooleanjintID ,id)) == JNI_TRUE;
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
}
  
void ScilabJavaObject2::unwrapRowBoolean (JavaVM * jvm_, int id, int pos){
  
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jobjectArray_unwrapRowBooleanjintID = curEnv->GetStaticMethodID(cls, "unwrapRowBoolean", "(I)[Z" ) ;
  if (jobjectArray_unwrapRowBooleanjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapRowBoolean");
    }
  
  jobjectArray res = static_cast<jobjectArray>(curEnv->CallStaticObjectMethod(cls, jobjectArray_unwrapRowBooleanjintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  
  jint lenRow = curEnv->GetArrayLength(res);
  jboolean isCopy = JNI_FALSE;
  
  /* GetPrimitiveArrayCritical is faster than getXXXArrayElements */
  int *addr;
  SciErr err = allocMatrixOfBoolean(pvApiCtx, pos, 1, lenRow, &addr);
  if (err.iErr)
    {
      throw "No more memory.";
    }

  jboolean *resultsArray = static_cast<jboolean *>(curEnv->GetPrimitiveArrayCritical(res, &isCopy));
  
  for (jsize i = 0; i < lenRow; i++)
    {
      addr[i] = (resultsArray[i] == JNI_TRUE);
    }
  curEnv->ReleasePrimitiveArrayCritical(res, resultsArray, JNI_ABORT);
  
  curEnv->DeleteLocalRef(res);
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
}
  
void ScilabJavaObject2::unwrapMatBoolean (JavaVM * jvm_, int id, int pos){
    
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jobjectArray__unwrapMatBooleanjintID = curEnv->GetStaticMethodID(cls, "unwrapMatBoolean", "(I)[[Z" ) ;
  if (jobjectArray__unwrapMatBooleanjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapMatBoolean");
    }
  
  jobjectArray res = static_cast<jobjectArray>(curEnv->CallStaticObjectMethod(cls, jobjectArray__unwrapMatBooleanjintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  jint lenRow = curEnv->GetArrayLength(res);
  jboolean isCopy = JNI_FALSE;
  
  jbooleanArray oneDim = (jbooleanArray)curEnv->GetObjectArrayElement(res, 0);
  jint lenCol=curEnv->GetArrayLength(oneDim);
  int *addr;

  SciErr err;
  if (methodOfConv)
    err = allocMatrixOfBoolean(pvApiCtx, pos, lenRow, lenCol, &addr);
  else
    err = allocMatrixOfBoolean(pvApiCtx, pos, lenCol, lenRow, &addr);
  
  if (err.iErr)
    {
      throw "No more memory.";
    }
  
  for (int i = 0; i < lenRow; i++) {
    oneDim = (jbooleanArray)curEnv->GetObjectArrayElement(res, i);
    jboolean *resultsArray = static_cast<jboolean *>(curEnv->GetPrimitiveArrayCritical(oneDim, &isCopy));
    if (methodOfConv)
      {
	for (int j = 0; j < lenCol; j++)
	  addr[j * lenRow + i] = (resultsArray[j] == JNI_TRUE);
      }
    else 
      {
	for (int j = 0; j < lenCol; j++)
	  {
	    addr[i * lenCol + j] = (resultsArray[j] == JNI_TRUE);
	  }
      }
    curEnv->ReleasePrimitiveArrayCritical(res, resultsArray, JNI_ABORT);
  }
  
  curEnv->DeleteLocalRef(res);
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
}
 }
