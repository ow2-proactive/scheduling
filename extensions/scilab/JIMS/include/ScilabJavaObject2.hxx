#ifndef __SCILABOBJECTS_SCILABJAVAOBJECT2__
#define __SCILABOBJECTS_SCILABJAVAOBJECT2__
#include <iostream>
#include <string>
#include <string.h>
#include <stdlib.h>
#include <jni.h>

#include "GiwsException.hxx"

#if !defined(bbyte)
typedef signed char bbyte;
#else
#prama message("Byte has been refined elsewhere. Some problems can happen")
#endif

extern char methodOfConv;

namespace ScilabObjects {

class ScilabJavaObject2 {
private:
JavaVM * jvm;
jobject instance;

jclass instanceClass; // cache class
jmethodID jobjectArray_getAccessibleMethodsjintID; // cache method id
jmethodID jobjectArray_getAccessibleFieldsjintID; // cache method id
jmethodID jdoubleunwrapDoublejintID; // cache method id
jmethodID jobjectArray_unwrapRowDoublejintID; // cache method id
jmethodID jobjectArray__unwrapMatDoublejintID; // cache method id
jmethodID jstringunwrapStringjintID; // cache method id
jmethodID jobjectArray_unwrapRowStringjintID; // cache method id
jmethodID jobjectArray__unwrapMatStringjintID; // cache method id
jmethodID jbooleanunwrapBooleanjintID; // cache method id
jmethodID jobjectArray_unwrapRowBooleanjintID; // cache method id
jmethodID jobjectArray__unwrapMatBooleanjintID; // cache method id
jmethodID jbyteunwrapBytejintID; // cache method id
jmethodID jobjectArray_unwrapRowBytejintID; // cache method id
jmethodID jobjectArray__unwrapMatBytejintID; // cache method id
jmethodID jshortunwrapShortjintID; // cache method id
jmethodID jobjectArray_unwrapRowShortjintID; // cache method id
jmethodID jobjectArray__unwrapMatShortjintID; // cache method id
jmethodID jintunwrapIntjintID; // cache method id
jmethodID jobjectArray_unwrapRowIntjintID; // cache method id
jmethodID jobjectArray__unwrapMatIntjintID; // cache method id
jmethodID jcharunwrapCharjintID; // cache method id
jmethodID jobjectArray_unwrapRowCharjintID; // cache method id
jmethodID jobjectArray__unwrapMatCharjintID; // cache method id
jmethodID jfloatunwrapFloatjintID; // cache method id
jmethodID jobjectArray_unwrapRowFloatjintID; // cache method id
jmethodID jobjectArray__unwrapMatFloatjintID; // cache method id
jmethodID jlongunwrapLongjintID; // cache method id
jmethodID jobjectArray_unwrapRowLongjintID; // cache method id
jmethodID jobjectArray__unwrapMatLongjintID; // cache method id
jclass stringArrayClass;

/**
* Get the environment matching to the current thread.
*/
JNIEnv * getCurrentEnv();

public:
// Constructor
/**
* Create a wrapping of the object from a JNIEnv.
* It will call the default constructor
* @param JEnv_ the Java Env
*/
ScilabJavaObject2(JavaVM * jvm_);
/**
* Create a wrapping of an already existing object from a JNIEnv.
* The object must have already been instantiated
* @param JEnv_ the Java Env
* @param JObj the object
*/
ScilabJavaObject2(JavaVM * jvm_, jobject JObj);

// Destructor
~ScilabJavaObject2();

// Generic method
// Synchronization methods
/**
* Enter monitor associated with the object.
* Equivalent of creating a "synchronized(obj)" scope in Java.
*/
void synchronize();

/**
* Exit monitor associated with the object.
* Equivalent of ending a "synchronized(obj)" scope.
*/
void endSynchronize();

// Methods
static void getAccessibleMethods(JavaVM * jvm_, int id, int pos);
static void getAccessibleFields(JavaVM * jvm_, int id, int pos);
static void unwrapDouble(JavaVM * jvm_, int id, int pos);
static void unwrapRowDouble(JavaVM * jvm_, int id, int pos);
static void unwrapMatDouble(JavaVM * jvm_, int id, int pos);
static void unwrapString(JavaVM * jvm_, int id, int pos);
static void unwrapRowString(JavaVM * jvm_, int id, int pos);
static void unwrapMatString(JavaVM * jvm_, int id, int pos);
static void unwrapBoolean(JavaVM * jvm_, int id, int pos);
static void unwrapRowBoolean(JavaVM * jvm_, int id, int pos);
static void unwrapMatBoolean(JavaVM * jvm_, int id, int pos);
static void unwrapByte(JavaVM * jvm_, int id, int pos);
static void unwrapRowByte(JavaVM * jvm_, int id, int pos);
static void unwrapMatByte(JavaVM * jvm_, int id, int pos);
static void unwrapShort(JavaVM * jvm_, int id, int pos);
static void unwrapRowShort(JavaVM * jvm_, int id, int pos);
static void unwrapMatShort(JavaVM * jvm_, int id, int pos);
static void unwrapInt(JavaVM * jvm_, int id, int pos);
static void unwrapRowInt(JavaVM * jvm_, int id, int pos);
static void unwrapMatInt(JavaVM * jvm_, int id, int pos);
static void unwrapChar(JavaVM * jvm_, int id, int pos);
static void unwrapRowChar(JavaVM * jvm_, int id, int pos);
static void unwrapMatChar(JavaVM * jvm_, int id, int pos);
static void unwrapFloat(JavaVM * jvm_, int id, int pos);
static void unwrapRowFloat(JavaVM * jvm_, int id, int pos);
static void unwrapMatFloat(JavaVM * jvm_, int id, int pos);
static void unwrapLong(JavaVM * jvm_, int id, int pos);
static void unwrapRowLong(JavaVM * jvm_, int id, int pos);
static void unwrapMatLong(JavaVM * jvm_, int id, int pos);

/**
 * Get class name to use for static methods
 * @return class name to use for static methods
 */
static const std::string className()
  {
    return "ScilabObjects/ScilabJavaObject";
  }  
};

}
#endif
