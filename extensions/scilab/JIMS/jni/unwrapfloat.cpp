#include "api_scilab.h"
#include "ScilabJavaObject2.hxx"

namespace ScilabObjects {

void ScilabJavaObject2::unwrapFloat (JavaVM * jvm_, int id, int pos){
    
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jfloatunwrapFloatjintID = curEnv->GetStaticMethodID(cls, "unwrapFloat", "(I)F" ) ;
  if (jfloatunwrapFloatjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapFloat");
    }

  double *addr;
  SciErr err = allocMatrixOfDouble(pvApiCtx, pos, 1, 1, &addr);
  if (err.iErr)
    {
      throw "No more memory.";
    }
    
  *addr = static_cast<double>(curEnv->CallStaticFloatMethod(cls, jfloatunwrapFloatjintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
}
  
void ScilabJavaObject2::unwrapRowFloat (JavaVM * jvm_, int id, int pos){
  
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jobjectArray_unwrapRowFloatjintID = curEnv->GetStaticMethodID(cls, "unwrapRowFloat", "(I)[F" ) ;
  if (jobjectArray_unwrapRowFloatjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapRowFloat");
    }
  
  jobjectArray res = static_cast<jobjectArray>(curEnv->CallStaticObjectMethod(cls, jobjectArray_unwrapRowFloatjintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  
  jint lenRow = curEnv->GetArrayLength(res);
  jboolean isCopy = JNI_FALSE;
  
  /* GetPrimitiveArrayCritical is faster than getXXXArrayElements */
  double *addr;
  SciErr err = allocMatrixOfDouble(pvApiCtx, pos, 1, lenRow, &addr);
  if (err.iErr)
    {
      throw "No more memory.";
    }

  jfloat *resultsArray = static_cast<jfloat *>(curEnv->GetPrimitiveArrayCritical(res, &isCopy));
    
  for (jsize i = 0; i < lenRow; i++)
    {
      addr[i] = (double) resultsArray[i];
    }
  curEnv->ReleasePrimitiveArrayCritical(res, resultsArray, JNI_ABORT);
  
  curEnv->DeleteLocalRef(res);
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  
}
  
void ScilabJavaObject2::unwrapMatFloat (JavaVM * jvm_, int id, int pos){
    
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jobjectArray__unwrapMatFloatjintID = curEnv->GetStaticMethodID(cls, "unwrapMatFloat", "(I)[[F" ) ;
  if (jobjectArray__unwrapMatFloatjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapMatFloat");
    }
  
  jobjectArray res = static_cast<jobjectArray>(curEnv->CallStaticObjectMethod(cls, jobjectArray__unwrapMatFloatjintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  jint lenRow = curEnv->GetArrayLength(res);
  jboolean isCopy = JNI_FALSE;
  
  jfloatArray oneDim = (jfloatArray)curEnv->GetObjectArrayElement(res, 0);
  jint lenCol = curEnv->GetArrayLength(oneDim);
  double * addr;

  SciErr err;
  if (methodOfConv)
    err = allocMatrixOfDouble(pvApiCtx, pos, lenRow, lenCol, &addr);
  else
    err = allocMatrixOfDouble(pvApiCtx, pos, lenCol, lenRow, &addr);

  if (err.iErr)
    {
      throw "No more memory.";
    }

  int s = sizeof(float) * lenCol;
  for(int i = 0; i < lenRow; i++) {
    oneDim = (jfloatArray)curEnv->GetObjectArrayElement(res, i);
    jfloat *resultsArray = static_cast<jfloat *>(curEnv->GetPrimitiveArrayCritical(oneDim, &isCopy));
    if (methodOfConv)
      {
	for (int j = 0; j < lenCol; j++)
	  addr[j * lenRow + i] = (double) resultsArray[j];
      }
    else 
      {
	for (int j = 0; j < lenCol; j++)
	  addr[i * lenCol + j] = (double) resultsArray[j];
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
