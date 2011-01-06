#include "api_scilab.h"
#include "ScilabJavaObject2.hxx"

namespace ScilabObjects {

void ScilabJavaObject2::unwrapLong (JavaVM * jvm_, int id, int pos){
    
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jlongunwrapLongjintID = curEnv->GetStaticMethodID(cls, "unwrapLong", "(I)J" ) ;
  if (jlongunwrapLongjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapLong");
    }

#ifndef __SCILAB_INT64__
  unsigned int *addr;
  SciErr err = allocMatrixOfUnsignedInteger32(pvApiCtx, pos, 1, 1, &addr);
  if (err.iErr)
    {
      throw "No more memory.";
    }

  *addr = static_cast<unsigned int>(curEnv->CallStaticLongMethod(cls, jlongunwrapLongjintID ,id));
#else
  long long *addr;
  SciErr err = allocMatrixOfInteger64(pvApiCtx, pos, 1, 1, &addr);
  if (err.iErr)
    {
      throw "No more memory.";
    }

  *addr = static_cast<long long>(curEnv->CallStaticLongMethod(cls, jlongunwrapLongjintID ,id));
#endif

  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
}
  
void ScilabJavaObject2::unwrapRowLong (JavaVM * jvm_, int id, int pos){
  
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jobjectArray_unwrapRowLongjintID = curEnv->GetStaticMethodID(cls, "unwrapRowLong", "(I)[J" ) ;
  if (jobjectArray_unwrapRowLongjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapRowLong");
    }
  
  jobjectArray res = static_cast<jobjectArray>(curEnv->CallStaticObjectMethod(cls, jobjectArray_unwrapRowLongjintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  
  jint lenRow = curEnv->GetArrayLength(res);
  jboolean isCopy = JNI_FALSE;
  
#ifndef __SCILAB_INT64__
  unsigned int *addr;
  SciErr err = allocMatrixOfUnsignedInteger32(pvApiCtx, pos, 1, lenRow, &addr);
  if (err.iErr)
    {
      throw "No more memory.";
    }
  
  jlong *resultsArray = static_cast<jlong *>(curEnv->GetPrimitiveArrayCritical(res, &isCopy));
  
  for (jsize i = 0; i < lenRow; i++)
    {
      addr[i] = (unsigned int) resultsArray[i];
    }
#else
  long long *addr;
  SciErr err = allocMatrixOfInteger64(pvApiCtx, pos, 1, lenRow, &addr);
  if (err.iErr)
    {
      throw "No more memory.";
    }
  
  jlong *resultsArray = static_cast<jlong *>(curEnv->GetPrimitiveArrayCritical(res, &isCopy));
    
  memcpy(addr, resultsArray, sizeof(long long) * lenRow);
#endif

  curEnv->ReleasePrimitiveArrayCritical(res, resultsArray, JNI_ABORT);
  
  curEnv->DeleteLocalRef(res);
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  
}
  
void ScilabJavaObject2::unwrapMatLong (JavaVM * jvm_, int id, int pos){
    
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jobjectArray__unwrapMatLongjintID = curEnv->GetStaticMethodID(cls, "unwrapMatLong", "(I)[[J" ) ;
  if (jobjectArray__unwrapMatLongjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapMatLong");
    }
  
  jobjectArray res = static_cast<jobjectArray>(curEnv->CallStaticObjectMethod(cls, jobjectArray__unwrapMatLongjintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  jint lenRow = curEnv->GetArrayLength(res);
  jboolean isCopy = JNI_FALSE;
  
  jlongArray oneDim = (jlongArray)curEnv->GetObjectArrayElement(res, 0);
  jint lenCol=curEnv->GetArrayLength(oneDim);
  
#ifndef __SCILAB_INT64__
  unsigned int *addr;

  SciErr err;
  if (methodOfConv)
    err = allocMatrixOfUnsignedInteger32(pvApiCtx, pos, lenRow, lenCol, &addr);
  else
    err = allocMatrixOfUnsignedInteger32(pvApiCtx, pos, lenCol, lenRow, &addr);
#else
  long long *addr;

  SciErr err;
  if (methodOfConv)
    err = allocMatrixOfInteger64(pvApiCtx, pos, lenRow, lenCol, &addr);
  else
    err = allocMatrixOfInteger64(pvApiCtx, pos, lenCol, lenRow, &addr);
#endif

  if (err.iErr)
    {
      throw "No more memory.";
    }
  
  int s = sizeof(unsigned int) * lenCol;
  for(int i=0; i < lenRow; i++) {
    oneDim = (jlongArray)curEnv->GetObjectArrayElement(res, i);
    jlong *resultsArray = static_cast<jlong *>(curEnv->GetPrimitiveArrayCritical(oneDim, &isCopy));
    if (methodOfConv)
      {
	for (int j = 0; j < lenCol; j++)
	  {
#ifndef __SCILAB_INT64__
	    addr[j * lenRow + i] = (unsigned int) resultsArray[j];
#else
	    addr[j * lenRow + i] = resultsArray[j];
#endif
	  }
      }
    else 
      {
#ifndef __SCILAB_INT64__
	for (int j = 0; j < lenCol; j++) 
	  {
	    addr[i * lenRow + j] = (unsigned int) resultsArray[j];
	  }
#else
	memcpy(addr, resultsArray, s);
	addr += lenCol;
#endif
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
