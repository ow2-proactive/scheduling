#include "api_scilab.h"
#include "ScilabJavaObject2.hxx"

namespace ScilabObjects {

void ScilabJavaObject2::unwrapInt (JavaVM * jvm_, int id, int pos){
    
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jintunwrapIntjintID = curEnv->GetStaticMethodID(cls, "unwrapInt", "(I)I" ) ;
  if (jintunwrapIntjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapInt");
    }
  int *addr;
  SciErr err = allocMatrixOfInteger32(pvApiCtx, pos, 1, 1, &addr);
  if (err.iErr)
    {
      throw "No more memory.";
    }

  *addr = static_cast<jint>( curEnv->CallStaticIntMethod(cls, jintunwrapIntjintID, id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
}
  
void ScilabJavaObject2::unwrapRowInt (JavaVM * jvm_, int id, int pos){
  
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jobjectArray_unwrapRowIntjintID = curEnv->GetStaticMethodID(cls, "unwrapRowInt", "(I)[I" ) ;
  if (jobjectArray_unwrapRowIntjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapRowInt");
    }
  
  jobjectArray res =  static_cast<jobjectArray>( curEnv->CallStaticObjectMethod(cls, jobjectArray_unwrapRowIntjintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  
  jint lenRow = curEnv->GetArrayLength(res);
  jboolean isCopy = JNI_FALSE;
  
  /* GetPrimitiveArrayCritical is faster than getXXXArrayElements */
  int *addr;
  SciErr err = allocMatrixOfInteger32(pvApiCtx, pos, 1, lenRow, &addr);
  if (err.iErr)
    {
      throw "No more memory.";
    }
  
  jint *resultsArray = static_cast<jint *>(curEnv->GetPrimitiveArrayCritical(res, &isCopy));
  
  memcpy(addr, resultsArray, sizeof(int) * lenRow);
  curEnv->ReleasePrimitiveArrayCritical(res, resultsArray, JNI_ABORT);
  
  curEnv->DeleteLocalRef(res);
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  
}
  
void ScilabJavaObject2::unwrapMatInt (JavaVM * jvm_, int id, int pos){
    
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jobjectArray__unwrapMatIntjintID = curEnv->GetStaticMethodID(cls, "unwrapMatInt", "(I)[[I" ) ;
  if (jobjectArray__unwrapMatIntjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapMatInt");
    }
  
  jobjectArray res =  static_cast<jobjectArray>( curEnv->CallStaticObjectMethod(cls, jobjectArray__unwrapMatIntjintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  jint lenRow = curEnv->GetArrayLength(res);
  jboolean isCopy = JNI_FALSE;
  jintArray oneDim = (jintArray)curEnv->GetObjectArrayElement(res, 0);
  jint lenCol=curEnv->GetArrayLength(oneDim);
  int *addr;

  SciErr err;
  if (methodOfConv)
    err = allocMatrixOfInteger32(pvApiCtx, pos, lenRow, lenCol, &addr);
  else
    err = allocMatrixOfInteger32(pvApiCtx, pos, lenCol, lenRow, &addr);
  
  if (err.iErr)
    {
      throw "No more memory.";
    }

  int s = sizeof(int) * lenCol;
  for(int i=0; i < lenRow; i++) {
    oneDim = (jintArray)curEnv->GetObjectArrayElement(res, i);
    int *resultsArray = static_cast<int *>(curEnv->GetPrimitiveArrayCritical(oneDim, &isCopy));
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
