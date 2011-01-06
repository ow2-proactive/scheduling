#include "api_scilab.h"
#include "ScilabJavaObject2.hxx"

namespace ScilabObjects {

void ScilabJavaObject2::unwrapChar (JavaVM * jvm_, int id, int pos){
    
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jcharunwrapCharjintID = curEnv->GetStaticMethodID(cls, "unwrapChar", "(I)C" ) ;
  if (jcharunwrapCharjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapChar");
    }

  unsigned short *addr;
  SciErr err = allocMatrixOfUnsignedInteger16(pvApiCtx, pos, 1, 1, &addr);
  if (err.iErr)
    {
      throw "No more memory.";
    }

  *addr = static_cast<unsigned short>(curEnv->CallStaticCharMethod(cls, jcharunwrapCharjintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
}
  
void ScilabJavaObject2::unwrapRowChar (JavaVM * jvm_, int id, int pos){
  
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jobjectArray_unwrapRowCharjintID = curEnv->GetStaticMethodID(cls, "unwrapRowChar", "(I)[C" ) ;
  if (jobjectArray_unwrapRowCharjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapRowChar");
    }
  
  jobjectArray res = static_cast<jobjectArray>(curEnv->CallStaticObjectMethod(cls, jobjectArray_unwrapRowCharjintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  
  jint lenRow = curEnv->GetArrayLength(res);
  jboolean isCopy = JNI_FALSE;
  
  /* GetPrimitiveArrayCritical is faster than getXXXArrayElements */
  unsigned short *addr;
  SciErr err = allocMatrixOfUnsignedInteger16(pvApiCtx, pos, 1, lenRow, &addr);
  if (err.iErr)
    {
      throw "No more memory.";
    }
    
  unsigned short *resultsArray = static_cast<unsigned short *>(curEnv->GetPrimitiveArrayCritical(res, &isCopy));
  
  memcpy(addr, resultsArray, sizeof(unsigned short) * lenRow);
  curEnv->ReleasePrimitiveArrayCritical(res, resultsArray, JNI_ABORT);
  
  curEnv->DeleteLocalRef(res);
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  
}
  
void ScilabJavaObject2::unwrapMatChar (JavaVM * jvm_, int id, int pos){
    
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jobjectArray__unwrapMatCharjintID = curEnv->GetStaticMethodID(cls, "unwrapMatChar", "(I)[[C" ) ;
  if (jobjectArray__unwrapMatCharjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapMatChar");
    }
  
  jobjectArray res = static_cast<jobjectArray>(curEnv->CallStaticObjectMethod(cls, jobjectArray__unwrapMatCharjintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  jint lenRow = curEnv->GetArrayLength(res);
  jboolean isCopy = JNI_FALSE;
  
  jcharArray oneDim = (jcharArray)curEnv->GetObjectArrayElement(res, 0);
  jint lenCol=curEnv->GetArrayLength(oneDim);
  unsigned short * addr;
  
  SciErr err;
  if (methodOfConv)
    err = allocMatrixOfUnsignedInteger16(pvApiCtx, pos, lenRow, lenCol, &addr);
  else
    err = allocMatrixOfUnsignedInteger16(pvApiCtx, pos, lenCol, lenRow, &addr);

  if (err.iErr)
    {
      throw "No more memory.";
    }

  int s = sizeof(unsigned short) * lenCol;
  for(int i = 0; i < lenRow; i++) {
    oneDim = (jcharArray)curEnv->GetObjectArrayElement(res, i);
    unsigned short *resultsArray = static_cast<unsigned short *>(curEnv->GetPrimitiveArrayCritical(oneDim, &isCopy));
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
