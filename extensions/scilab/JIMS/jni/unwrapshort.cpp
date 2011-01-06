#include "api_scilab.h"
#include "ScilabJavaObject2.hxx"

namespace ScilabObjects {

void ScilabJavaObject2::unwrapShort (JavaVM * jvm_, int id, int pos){
    
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jshortunwrapShortjintID = curEnv->GetStaticMethodID(cls, "unwrapShort", "(I)S" ) ;
  if (jshortunwrapShortjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapShort");
    }

  short *addr;
  SciErr err = allocMatrixOfInteger16(pvApiCtx, pos, 1, 1, &addr);
  if (err.iErr)
    {
      throw "No more memory.";
    }

  *addr = static_cast<jshort>(curEnv->CallStaticShortMethod(cls, jshortunwrapShortjintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
}
  
void ScilabJavaObject2::unwrapRowShort (JavaVM * jvm_, int id, int pos){
  
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jobjectArray_unwrapRowShortjintID = curEnv->GetStaticMethodID(cls, "unwrapRowShort", "(I)[S" ) ;
  if (jobjectArray_unwrapRowShortjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapRowShort");
    }
  
  jobjectArray res = static_cast<jobjectArray>(curEnv->CallStaticObjectMethod(cls, jobjectArray_unwrapRowShortjintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  
  jint lenRow = curEnv->GetArrayLength(res);
  jboolean isCopy = JNI_FALSE;
  
  /* GetPrimitiveArrayCritical is faster than getXXXArrayElements */
  short *addr;
  SciErr err = allocMatrixOfInteger16(pvApiCtx, pos, 1, lenRow, &addr);
  if (err.iErr)
    {
      throw "No more memory.";
    }

  jshort *resultsArray = static_cast<jshort *>(curEnv->GetPrimitiveArrayCritical(res, &isCopy));
  
  memcpy(addr, resultsArray, sizeof(short) * lenRow);
  curEnv->ReleasePrimitiveArrayCritical(res, resultsArray, JNI_ABORT);
  
  curEnv->DeleteLocalRef(res);
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  
}
  
void ScilabJavaObject2::unwrapMatShort (JavaVM * jvm_, int id, int pos){
    
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jobjectArray__unwrapMatShortjintID = curEnv->GetStaticMethodID(cls, "unwrapMatShort", "(I)[[S" ) ;
  if (jobjectArray__unwrapMatShortjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapMatShort");
    }
  
  jobjectArray res = static_cast<jobjectArray>(curEnv->CallStaticObjectMethod(cls, jobjectArray__unwrapMatShortjintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  jint lenRow = curEnv->GetArrayLength(res);
  jboolean isCopy = JNI_FALSE;
  
  jshortArray oneDim = (jshortArray)curEnv->GetObjectArrayElement(res, 0);
  jint lenCol=curEnv->GetArrayLength(oneDim);
  short * addr;

  SciErr err;
  if (methodOfConv)
    err = allocMatrixOfInteger16(pvApiCtx, pos, lenRow, lenCol, &addr);
  else
    err = allocMatrixOfInteger16(pvApiCtx, pos, lenCol, lenRow, &addr);

  if (err.iErr)
    {
      throw "No more memory.";
    }

  int s = sizeof(short) * lenCol;
  for(int i=0; i < lenRow; i++) {
    oneDim = (jshortArray)curEnv->GetObjectArrayElement(res, i);
    short *resultsArray = static_cast<short *>(curEnv->GetPrimitiveArrayCritical(oneDim, &isCopy));
    if (methodOfConv)
      {
	for (int j = 0; j < lenCol; j++)
	  addr[j * lenRow + i] = resultsArray[j];
      }
    else 
      {
	memcpy(addr, resultsArray, s);
	addr += lenCol;
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
