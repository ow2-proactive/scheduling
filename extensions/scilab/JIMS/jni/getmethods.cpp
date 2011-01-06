#include "api_scilab.h"
#include "ScilabJavaObject2.hxx"

namespace ScilabObjects {

void ScilabJavaObject2::getAccessibleMethods (JavaVM * jvm_, int id, int pos){

  JNIEnv * curEnv = NULL;
  jvm_->AttachCurrentThread(reinterpret_cast<void **>(&curEnv), NULL);
  jclass cls = curEnv->FindClass(className().c_str());
  
  jmethodID jobjectArray_getAccessibleMethodsjintID = curEnv->GetStaticMethodID(cls, "getAccessibleMethods", "(I)[Ljava/lang/String;");
  if (jobjectArray_getAccessibleMethodsjintID == NULL)
    {
      throw GiwsException::JniMethodNotFoundException(curEnv, "getAccessibleMethods");
    }
  
  jobjectArray res = static_cast<jobjectArray>(curEnv->CallStaticObjectMethod(cls, jobjectArray_getAccessibleMethodsjintID, id));
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
}
