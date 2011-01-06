#include "api_scilab.h"
#include "ScilabJavaObject2.hxx"

namespace ScilabObjects {

void ScilabJavaObject2::unwrapString (JavaVM * jvm_, int id, int pos){
    
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jstringunwrapStringjintID = curEnv->GetStaticMethodID(cls, "unwrapString", "(I)Ljava/lang/String;" ) ;
  if (jstringunwrapStringjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapString");
    }

  jstring res = static_cast<jstring>(curEnv->CallStaticObjectMethod(cls, jstringunwrapStringjintID ,id));
  char *addr = const_cast<char *>(curEnv->GetStringUTFChars(res, 0));
  SciErr err = createMatrixOfString(pvApiCtx, pos, 1, 1, &addr);
  if (err.iErr)
    {
      throw "No more memory.";
    }
  
  curEnv->ReleaseStringUTFChars(res, addr);
  curEnv->DeleteLocalRef(res);
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
}
  
void ScilabJavaObject2::unwrapRowString (JavaVM * jvm_, int id, int pos){
  
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jobjectArray_unwrapRowStringjintID = curEnv->GetStaticMethodID(cls, "unwrapRowString", "(I)[Ljava/lang/String;" );
  if (jobjectArray_unwrapRowStringjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapRowString");
    }
  
  jobjectArray res = static_cast<jobjectArray>(curEnv->CallStaticObjectMethod(cls, jobjectArray_unwrapRowStringjintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  
  jint lenRow = curEnv->GetArrayLength(res);
  jboolean isCopy = JNI_FALSE;
  char **addr = new char*[lenRow];
  jstring *resString = new jstring[lenRow];

  for (jsize i = 0; i < lenRow; i++)
    {
      resString[i] = reinterpret_cast<jstring>(curEnv->GetObjectArrayElement(res, i));
      addr[i] = const_cast<char *>(curEnv->GetStringUTFChars(resString[i], &isCopy));
    }

  SciErr err = createMatrixOfString(pvApiCtx, pos, 1, lenRow, addr);

  for (jsize i = 0; i < lenRow; i++)
    {
      curEnv->ReleaseStringUTFChars(resString[i], addr[i]);
      curEnv->DeleteLocalRef(resString[i]);
    }
  delete addr;
  delete resString;

  if (err.iErr)
    {
      throw "No more memory.";
    }

  curEnv->DeleteLocalRef(res);
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }  
}
  
void ScilabJavaObject2::unwrapMatString (JavaVM * jvm_, int id, int pos){
    
  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass( className().c_str() );
  
  jmethodID jobjectArray__unwrapMatStringjintID = curEnv->GetStaticMethodID(cls, "unwrapMatString", "(I)[[Ljava/lang/String;" ) ;
  if (jobjectArray__unwrapMatStringjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "unwrapMatString");
    }
  
  jobjectArray res = static_cast<jobjectArray>(curEnv->CallStaticObjectMethod(cls, jobjectArray__unwrapMatStringjintID ,id));
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
  jint lenRow = curEnv->GetArrayLength(res);
  jboolean isCopy = JNI_FALSE;
  
  jobjectArray oneDim = reinterpret_cast<jobjectArray>(curEnv->GetObjectArrayElement(res, 0));
  jint lenCol=curEnv->GetArrayLength(oneDim);
  char **addr = new char*[lenRow * lenCol];
  jstring *resString = new jstring[lenRow * lenCol];

  for (int i = 0; i < lenRow; i++)
    {
      oneDim = reinterpret_cast<jobjectArray>(curEnv->GetObjectArrayElement(res, i));
      if (methodOfConv)
	{
	  for (int j = 0; j < lenCol; j++)
	    {
	      resString[j * lenRow + i] = reinterpret_cast<jstring>(curEnv->GetObjectArrayElement(oneDim, j));
	      addr[j * lenRow + i] = const_cast<char *>(curEnv->GetStringUTFChars(resString[j * lenRow + i], &isCopy));
	    }
	}
      else 
	{
	  for (int j = 0; j < lenCol; j++)
	    {
	      resString[i * lenCol + j] = reinterpret_cast<jstring>(curEnv->GetObjectArrayElement(oneDim, j));
	      addr[i * lenCol + j] = const_cast<char *>(curEnv->GetStringUTFChars(resString[i * lenCol + j], &isCopy));
	    }
	}
      
    }
  
  SciErr err;
  if (methodOfConv)
    err = createMatrixOfString(pvApiCtx, pos, lenRow, lenCol, addr);
  else
    err = createMatrixOfString(pvApiCtx, pos, lenCol, lenRow, addr);

  for (int i = 0; i < lenRow * lenCol; i++)
    {
      curEnv->ReleaseStringUTFChars(resString[i], addr[i]);
      curEnv->DeleteLocalRef(resString[i]);
    }
  delete addr;
  delete resString;

  if (err.iErr)
    {
      throw "No more memory.";
    }
  
  curEnv->DeleteLocalRef(res);
  if (curEnv->ExceptionCheck())
    {
      throw GiwsException::JniCallMethodException(curEnv);
    }
}
}
