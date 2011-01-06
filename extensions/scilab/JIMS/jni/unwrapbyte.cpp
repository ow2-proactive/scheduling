#include "api_scilab.h"
#include "ScilabJavaObject2.hxx"

namespace ScilabObjects {

void ScilabJavaObject2::unwrapByte (JavaVM * jvm_, int id, int pos){
    
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jbyteunwrapBytejintID = curEnv->GetStaticMethodID(cls, "unwrapByte", "(I)B" ) ;
  if (jbyteunwrapBytejintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapByte");
    }

  char *addr;
  SciErr err = allocMatrixOfInteger8(pvApiCtx, pos, 1, 1, &addr);
  if (err.iErr)
    {
      throw "No more memory.";
    }

  *addr = static_cast<char>(curEnv->CallStaticByteMethod(cls, jbyteunwrapBytejintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
}
  
void ScilabJavaObject2::unwrapRowByte (JavaVM * jvm_, int id, int pos){
  
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jobjectArray_unwrapRowBytejintID = curEnv->GetStaticMethodID(cls, "unwrapRowByte", "(I)[B" ) ;
  if (jobjectArray_unwrapRowBytejintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapRowByte");
    }
  
  jobjectArray res = static_cast<jobjectArray>(curEnv->CallStaticObjectMethod(cls, jobjectArray_unwrapRowBytejintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  
  jint lenRow = curEnv->GetArrayLength(res);
  jboolean isCopy = JNI_FALSE;
  
  /* GetPrimitiveArrayCritical is faster than getXXXArrayElements */
  char *addr;
  SciErr err = allocMatrixOfInteger8(pvApiCtx, pos, 1, lenRow, &addr);
  if (err.iErr)
    {
      throw "No more memory.";
    }
  
  char *resultsArray = static_cast<char *>(curEnv->GetPrimitiveArrayCritical(res, &isCopy));
  
  memcpy(addr, resultsArray, sizeof(char) * lenRow);
  curEnv->ReleasePrimitiveArrayCritical(res, resultsArray, JNI_ABORT);
  
  curEnv->DeleteLocalRef(res);
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  
}
  
void ScilabJavaObject2::unwrapMatByte (JavaVM * jvm_, int id, int pos){
    
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jobjectArray__unwrapMatBytejintID = curEnv->GetStaticMethodID(cls, "unwrapMatByte", "(I)[[B" ) ;
  if (jobjectArray__unwrapMatBytejintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapMatByte");
    }
  
  jobjectArray res = static_cast<jobjectArray>(curEnv->CallStaticObjectMethod(cls, jobjectArray__unwrapMatBytejintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  jint lenRow = curEnv->GetArrayLength(res);
  jboolean isCopy = JNI_FALSE;
  
  jbyteArray oneDim = (jbyteArray)curEnv->GetObjectArrayElement(res, 0);
  jint lenCol=curEnv->GetArrayLength(oneDim);
  char * addr;

  SciErr err;
  if (methodOfConv)
    err = allocMatrixOfInteger8(pvApiCtx, pos, lenRow, lenCol, &addr);
  else
    err = allocMatrixOfInteger8(pvApiCtx, pos, lenCol, lenRow, &addr);

  if (err.iErr)
    {
      throw "No more memory.";
    }

  int s = sizeof(char) * lenCol;
  for(int i = 0; i < lenRow; i++) {
    oneDim = (jbyteArray)curEnv->GetObjectArrayElement(res, i);
    char *resultsArray = static_cast<char *>(curEnv->GetPrimitiveArrayCritical(oneDim, &isCopy));
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
