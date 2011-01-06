#include "api_scilab.h"
#include "ScilabJavaObject2.hxx"

namespace ScilabObjects {

void ScilabJavaObject2::unwrapDouble (JavaVM * jvm_, int id, int pos){
    
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jdoubleunwrapDoublejintID = curEnv->GetStaticMethodID(cls, "unwrapDouble", "(I)D" ) ;
  if (jdoubleunwrapDoublejintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapDouble");
    }

  double *addr;
  SciErr err = allocMatrixOfDouble(pvApiCtx, pos, 1, 1, &addr);
  if (err.iErr)
    {
      throw "No more memory.";
    }

  *addr = static_cast<jdouble>(curEnv->CallStaticDoubleMethod(cls, jdoubleunwrapDoublejintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
}
  
void ScilabJavaObject2::unwrapRowDouble (JavaVM * jvm_, int id, int pos){
  
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jobjectArray_unwrapRowDoublejintID = curEnv->GetStaticMethodID(cls, "unwrapRowDouble", "(I)[D" ) ;
  if (jobjectArray_unwrapRowDoublejintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapRowDouble");
    }
  
  jobjectArray res = static_cast<jobjectArray>(curEnv->CallStaticObjectMethod(cls, jobjectArray_unwrapRowDoublejintID ,id));
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

  jdouble *resultsArray = static_cast<jdouble *>(curEnv->GetPrimitiveArrayCritical(res, &isCopy));
  
  memcpy(addr, resultsArray, sizeof(double) * lenRow);
  curEnv->ReleasePrimitiveArrayCritical(res, resultsArray, JNI_ABORT);
  
  curEnv->DeleteLocalRef(res);
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  
}
  
void ScilabJavaObject2::unwrapMatDouble (JavaVM * jvm_, int id, int pos){
    
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jobjectArray__unwrapMatDoublejintID = curEnv->GetStaticMethodID(cls, "unwrapMatDouble", "(I)[[D" ) ;
  if (jobjectArray__unwrapMatDoublejintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapMatDouble");
    }
  
  jobjectArray res = static_cast<jobjectArray>(curEnv->CallStaticObjectMethod(cls, jobjectArray__unwrapMatDoublejintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  jint lenRow = curEnv->GetArrayLength(res);
  jboolean isCopy = JNI_FALSE;
  
  jdoubleArray oneDim = (jdoubleArray)curEnv->GetObjectArrayElement(res, 0);
  jint lenCol=curEnv->GetArrayLength(oneDim);
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

  int s = sizeof(double) * lenCol;
  for(int i=0; i < lenRow; i++) {
    oneDim = (jdoubleArray)curEnv->GetObjectArrayElement(res, i);
    double *resultsArray = static_cast<double *>(curEnv->GetPrimitiveArrayCritical(oneDim, &isCopy));
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
