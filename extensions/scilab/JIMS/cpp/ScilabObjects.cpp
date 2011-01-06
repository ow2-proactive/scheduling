#include "ScilabObjects.hxx"
#include "ScilabClassLoader.hxx"
#include "ScilabJavaClass.hxx"
#include "ScilabJavaObject.hxx"
#include "ScilabJavaObject2.hxx"
#include "ScilabJavaArray.hxx"
#include "GiwsException.hxx"
#include "stack-def.h"
#include "api_scilab.h"
#include "MALLOC.h"

extern "C" {
#include "getScilabJavaVM.h"
#include "stack-def.h"
}

using namespace ScilabObjects;

void
garbagecollect()
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      ScilabJavaObject::garbageCollect(vm);
    }
}

int
createjavaarray(char *className, int* dims, int len, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  return ScilabJavaArray::newInstance(vm, className, dims, len);
	}
      catch (GiwsException::JniException e)
	{
	  *errmsg = strdup(e.getJavaDescription().c_str());
	  return -1;
	}
    }

  return -1;
}

int
loadjavaclass (char *className, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  return ScilabClassLoader::loadJavaClass(vm, className);
	}
      catch (GiwsException::JniException e)
	{
	  *errmsg = strdup(e.getJavaDescription().c_str());
	  return -1;
	}
    }

  return -1;
}

char*
getrepresentation (int id, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  return ScilabJavaObject::getRepresentation(vm, id);
	}
      catch (GiwsException::JniException e)
	{
	  *errmsg = strdup(e.getJavaDescription().c_str());
	  return NULL;
	}
    }

  return NULL;
}

int
newinstance (int id, int *args, int argsSize, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  return ScilabJavaClass::newInstance(vm, id, args, argsSize);
	}
      catch (GiwsException::JniException e)
	{
	  *errmsg = strdup(e.getJavaDescription().c_str());
	  return -1;
	}
    }
  
  return -1;
}

int
invoke (int id, char *methodName, int *args, int argsSize, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  return ScilabJavaObject::invoke(vm, id, methodName, args, argsSize);
	}
      catch (GiwsException::JniException e)
	{
	  *errmsg = strdup(e.getJavaDescription().c_str());
	  return -1;
	}
    }
  
  return -1;
}

void
setfield (int id, char *fieldName, int idarg, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  return ScilabJavaObject::setField(vm, id, fieldName, idarg);
	}
      catch (GiwsException::JniException e)
	{
	  *errmsg = strdup(e.getJavaDescription().c_str());
	  return;
	}
    }
  
  return;
}

int
getfield (int id, char *fieldName, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  return ScilabJavaObject::getField(vm, id, fieldName);
	}
      catch (GiwsException::JniException e)
	{
	  *errmsg = strdup(e.getJavaDescription().c_str());
	  return -1;
	}
    }
  
  return -1;
}

int
javacast (int id, char *objName, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  return ScilabJavaObject::javaCast(vm, id, objName);
	}
      catch (GiwsException::JniException e)
	{
	  *errmsg = strdup(e.getJavaDescription().c_str());
	  return -1;
	}
    }
  
  return -1;
}

void
removescilabjavaobject (int id)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      ScilabJavaObject::removeScilabJavaObject(vm, id);
    }
}

void
getaccessiblemethods (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try 
	{
	  ScilabJavaObject2::getAccessibleMethods(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
getaccessiblefields (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try 
	{
	  ScilabJavaObject2::getAccessibleFields(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

int
wrapSingleDouble (double x)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapDouble(vm, x);
    }
  
  return -1;
}

int
wrapRowDouble (double *x, int len)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapDouble(vm, x, len);
    }
  
  return -1;
}

int
wrapMatDouble (double *x, int r, int c)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      if (methodOfConv)
	{
	  double **xx = new double*[r];
	  int i, j;
	  for (i = 0; i < r; i++)
	    {
	      xx[i] = new double[c];
	      for (j = 0; j < c; j++)
		{
		  xx[i][j] = x[j * r + i];
		}
	    }
	  j = ScilabJavaObject::wrapDouble(vm, xx, r, c);
	  for (i = 0; i < r; delete [] xx[i++]);
	  delete [] xx;
	  return j;
	}
      else
	{
	  double **xx = new double*[c];
	  int i;
	  xx[0] = x;
	  for (i = 1; i < c; xx[i] = xx[i++ - 1] + r);
	  i = ScilabJavaObject::wrapDouble(vm, xx, c, r);
	  delete [] xx;
	  return i;
	}
    }
  return -1;
}

int
wrapSingleInt (int x)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapInt(vm, x);
    }
  
  return -1;
}

int
wrapRowInt (int *x, int len)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapInt(vm, x, len);
    }
  
  return -1;
}

int
wrapMatInt (int *x, int r, int c)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      if (methodOfConv)
	{
	  int **xx = new int*[r];
	  int i, j;
	  for (i = 0; i < r; i++)
	    {
	      xx[i] = new int[c];
	      for (j = 0; j < c; j++)
		{
		  xx[i][j] = x[j * r + i];
		}
	    }
	  j = ScilabJavaObject::wrapInt(vm, xx, r, c);
	  for (i = 0; i < r; delete [] xx[i++]);
	  delete [] xx;
	  return j;
	}
      else
	{
	  int **xx = new int*[c];
	  int i;
	  xx[0] = x;
	  for (i = 1; i < c; xx[i] = xx[i++ - 1] + r);
	  i = ScilabJavaObject::wrapInt(vm, xx, c, r);
	  delete [] xx;
	  return i;
	}
    }
  return -1;
}

int
wrapSingleUInt (unsigned int x)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapUInt(vm, (long long)x);
    }
  
  return -1;
}

int
wrapRowUInt (unsigned int *x, int len)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      int i;
      long long *l = new long long[len];
      for (i = 0; i < len ; i++)
	{
	  l[i] = (long long)x[i];
	}
      return ScilabJavaObject::wrapUInt(vm, l, len);
    }
  
  return -1;
}

int
wrapMatUInt (unsigned int *x, int r, int c)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      if (methodOfConv)
	{
	  long long **xx = new long long*[r];
	  int i, j;
	  for (i = 0; i < r; i++)
	    {
	      xx[i] = new long long[c];
	      for (j = 0; j < c; j++)
		{
		  xx[i][j] = (long long)x[j * r + i];
		}
	    }
	  j = ScilabJavaObject::wrapUInt(vm, xx, r, c);
	  for (i = 0; i < r; delete [] xx[i++]);
	  delete [] xx;
	  return j;
	}
      else
	{
	  long long **xx = new long long*[c];
	  int i, j;
	  for (i = 0; i < c; i++)
	    {
	      xx[i] = new long long[r];
	      for (j = 0; j < r; j++)
		{
		  xx[i][j] = (long long)x[i * r + j];
		}
	    }
	  j = ScilabJavaObject::wrapUInt(vm, xx, c, r);
	  for (i = 0; i < c; delete [] xx[i++]);
	  delete [] xx;
	  return j;
	}
    }
  return -1;
}

int
wrapSingleByte (bbyte x)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapByte(vm, x);
    }
  
  return -1;
}

int
wrapRowByte (bbyte *x, int len)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapByte(vm, x, len);
    }
  
  return -1;
}

int
wrapMatByte (bbyte *x, int r, int c)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      if (methodOfConv)
	{
	  bbyte **xx = new bbyte*[r];
	  int i, j;
	  for (i = 0; i < r; i++)
	    {
	      xx[i] = new bbyte[c];
	      for (j = 0; j < c; j++)
		{
		  xx[i][j] = x[j * r + i];
		}
	    }
	  j = ScilabJavaObject::wrapByte(vm, xx, r, c);
	  for (i = 0; i < r; delete [] xx[i++]);
	  delete [] xx;
	  return j;
	}
      else
	{
	  bbyte **xx = new bbyte*[c];
	  int i;
	  xx[0] = x;
	  for (i = 1; i < c; xx[i] = xx[i++ - 1] + r);
	  i = ScilabJavaObject::wrapByte(vm, xx, c, r);
	  delete [] xx;
	  return i;
	}
    }
  return -1;
}

int
wrapSingleUByte (unsigned char x)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapUByte(vm, (short)x);
    }
  
  return -1;
}

int
wrapRowUByte (unsigned char *x, int len)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      int i;
      short *l = new short[len];
      for (i = 0; i < len; i++)
	{
	  l[i] = (short)x[i];
	}
      return ScilabJavaObject::wrapUByte(vm, l, len);
    }
  
  return -1;
}

int
wrapMatUByte (unsigned char *x, int r, int c)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      if (methodOfConv)
	{
	  short **xx = new short*[r];
	  int i, j;
	  for (i = 0; i < r; i++)
	    {
	      xx[i] = new short[c];
	      for (j = 0; j < c; j++)
		{
		  xx[i][j] = (short)x[j * r + i];
		}
	    }
	  j = ScilabJavaObject::wrapUByte(vm, xx, r, c);
	  for (i = 0; i < r; delete [] xx[i++]);
	  delete [] xx;
	  return j;
	}
      else
	{
	  short **xx = new short*[c];
	  int i, j;
	  for (i = 0; i < c; i++)
	    {
	      xx[i] = new short[r];
	      for (j = 0; j < r; j++)
		{
		  xx[i][j] = (short)x[i * r + j];
		}
	    }
	  j = ScilabJavaObject::wrapUByte(vm, xx, c, r);
	  for (i = 0; i < c; delete [] xx[i++]);
	  delete [] xx;
	  return j;
	}
    }
  return -1;
}

int
wrapSingleShort (short x)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapShort(vm, x);
    }
  
  return -1;
}

int
wrapRowShort (short *x, int len)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapShort(vm, x, len);
    }
  
  return -1;
}

int
wrapMatShort (short *x, int r, int c)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      if (methodOfConv)
	{
	  short **xx = new short*[r];
	  int i, j;
	  for (i = 0; i < r; i++)
	    {
	      xx[i] = new short[c];
	      for (j = 0; j < c; j++)
		{
		  xx[i][j] = x[j * r + i];
		}
	    }
	  j = ScilabJavaObject::wrapShort(vm, xx, r, c);
	  for (i = 0; i < r; delete [] xx[i++]);
	  delete [] xx;
	  return j;
	}
      else
	{
	  short **xx = new short*[c];
	  int i;
	  xx[0] = x;
	  for (i = 1; i < c; xx[i] = xx[i++ - 1] + r);
	  i = ScilabJavaObject::wrapShort(vm, xx, c, r);
	  delete [] xx;
	  return i;
	}
    }
  return -1;
}

int
wrapSingleUShort (unsigned short x)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapUShort(vm, (int)x);
    }
  
  return -1;
}

int
wrapRowUShort (unsigned short *x, int len)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      int i;
      int *l = new int[len];
      for (i = 0; i < len; i++)
	{
	  l[i] = (int)x[i];
	}
      return ScilabJavaObject::wrapUShort(vm, l, len);
    }
  
  return -1;
}

int
wrapMatUShort (unsigned short *x, int r, int c)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      if (methodOfConv)
	{
	  int **xx = new int*[r];
	  int i, j;
	  for (i = 0; i < r; i++)
	    {
	      xx[i] = new int[c];
	      for (j = 0; j < c; j++)
		{
		  xx[i][j] = (int)x[j * r + i];
		}
	    }
	  j = ScilabJavaObject::wrapUShort(vm, xx, r, c);
	  for (i = 0; i < r; delete [] xx[i++]);
	  delete [] xx;
	  return j;
	}
      else
	{
	  int **xx = new int*[c];
	  int i, j;
	  for (i = 0; i < c; i++)
	    {
	      xx[i] = new int[r];
	      for (j = 0; j < r; j++)
		{
		  xx[i][j] = (int)x[i * r + j];
		}
	    }
	  j = ScilabJavaObject::wrapUShort(vm, xx, c, r);
	  for (i = 0; i < c; delete [] xx[i++]);
	  delete [] xx;
	  return j;
	}
    }
  return -1;
}

int
wrapSingleString (char *x)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapString(vm, x);
    }
  
  return -1;
}

int
wrapRowString (char **x, int len)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapString(vm, x, len);
    }
  
  return -1;
}

int
wrapMatString (char **x, int r, int c)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      if (methodOfConv)
	{
	  char ***xx = new char**[r];
	  int i, j, k;
	  for (i = 0; i < r; i++)
	    {
	      xx[i] = new char*[c];
	      for (j = 0; j < c; j++)
		{
		  int len = strlen(x[j * r + i]) + 1;
		  xx[i][j] = new char[len];
		  memcpy(xx[i][j], x[j * r + i], len);
		}
	    }
	  k = ScilabJavaObject::wrapString(vm, xx, r, c);
	  for (i = 0; i < r; i++)
	    {
	      for (j = 0; j < c; j++)
		{
		  delete [] xx[i][j];
		}
	      delete [] xx[i];
	    }
	  delete [] xx;
	  return k;
	}
      else
	{
	  char ***xx = new char**[c];
	  int i;
	  xx[0] = x;
	  for (i = 1; i < c; xx[i] = xx[i++ - 1] + r);
	  i = ScilabJavaObject::wrapString(vm, xx, c, r);
	  delete [] xx;
	  return i;
	}
    }
  return -1;
}

int
wrapSingleBoolean (int x)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapBoolean(vm, x ? true : false);
    }

  return -1;
}

int
wrapRowBoolean (int *x, int len)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      bool *arr = new bool[len];
      int i;
      for (i = 0; i < len; i++)
	{
	  arr[i] = x[i] ? true : false;
	}
      return ScilabJavaObject::wrapBoolean(vm, arr, len);
    }

  return -1;
}

int
wrapMatBoolean (int *x, int r, int c)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      if (methodOfConv)
	{
	  bool **xx = new bool*[r];
	  int i, j;
	  for (i = 0; i < r; i++)
	    {
	      xx[i] = new bool[c];
	      for (j = 0; j < c; j++)
		{
		  xx[i][j] = (bool)x[j * r + i];
		}
	    }
	  j = ScilabJavaObject::wrapBoolean(vm, xx, r, c);
	  for (i = 0; i < r; delete [] xx[i++]);
	  delete [] xx;
	  return j;
	}
      else
	{
	  bool **xx = new bool*[c];
	  int i, j;
	  for (i = 0; i < c; i++)
	    {
	      xx[i] = new bool[r];
	      for (j = 0; j < r; j++)
		{
		  xx[i][j] = (bool)x[i * r + j];
		}
	    }
	  j = ScilabJavaObject::wrapBoolean(vm, xx, c, r);
	  for (i = 0; i < c; delete [] xx[i++]);
	  delete [] xx;
	  return j;
	}
    }
  return -1;
}

int
wrapSingleChar (unsigned short x)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapChar(vm, (int)x);
    }
  
  return -1;
}

int
wrapRowChar (unsigned short *x, int len)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapChar(vm, x, len);
    }
  
  return -1;
}

int
wrapMatChar (unsigned short *x, int r, int c)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      if (methodOfConv)
	{
	  unsigned short **xx = new unsigned short*[r];
	  int i, j;
	  for (i = 0; i < r; i++)
	    {
	      xx[i] = new unsigned short[c];
	      for (j = 0; j < c; j++)
		{
		  xx[i][j] = x[j * r + i];
		}
	    }
	  j = ScilabJavaObject::wrapChar(vm, xx, r, c);
	  for (i = 0; i < r; delete [] xx[i++]);
	  delete [] xx;
	  return j;
	}
      else
	{
	  unsigned short **xx = new unsigned short*[c];
	  int i;
	  xx[0] = x;
	  for (i = 1; i < c; xx[i] = xx[i++ - 1] + r);
	  i = ScilabJavaObject::wrapChar(vm, xx, c, r);
	  delete [] xx;
	  return i;
	}
    }
  return -1;
}

int
wrapSingleFloat (double x)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapFloat(vm, (float)x);
    }
  
  return -1;
}

int
wrapRowFloat (double *x, int len)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      int i;
      float *l = new float[len];
      for (i = 0; i < len ; i++)
	{
	  l[i] = (float)x[i];
	}
      return ScilabJavaObject::wrapFloat(vm, l, len);
    }
  
  return -1;
}

int
wrapMatFloat (double *x, int r, int c)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      if (methodOfConv)
	{
	  float **xx = new float*[r];
	  int i, j;
	  for (i = 0; i < r; i++)
	    {
	      xx[i] = new float[c];
	      for (j = 0; j < c; j++)
		{
		  xx[i][j] = (float)x[j * r + i];
		}
	    }
	  j = ScilabJavaObject::wrapFloat(vm, xx, r, c);
	  for (i = 0; i < r; delete [] xx[i++]);
	  delete [] xx;
	  return j;
	}
      else
	{
	  float **xx = new float*[c];
	  int i, j;
	  for (i = 0; i < c; i++)
	    {
	      xx[i] = new float[r];
	      for (j = 0; j < r; j++)
		{
		  xx[i][j] = (float)x[i * r + j];
		}
	    }
	  j = ScilabJavaObject::wrapFloat(vm, xx, c, r);
	  for (i = 0; i < c; delete [] xx[i++]);
	  delete [] xx;
	  return j;
	}
    }
  return -1;
}

#ifdef __SCILAB_INT64__
int
wrapSingleLong (long long x)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapLong(vm, x);
    }
  
  return -1;
}

int
wrapRowLong (long long *x, int len)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::wrapLong(vm, x, len);
    }
  
  return -1;
}

int
wrapMatLong (long long *x, int r, int c)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      if (methodOfConv)
	{
	  long long **xx = new long long*[r];
	  int i, j;
	  for (i = 0; i < r; i++)
	    {
	      xx[i] = new long long[c];
	      for (j = 0; j < c; j++)
		{
		  xx[i][j] = x[j * r + i];
		}
	    }
	  j = ScilabJavaObject::wrapLong(vm, xx, r, c);
	  for (i = 0; i < r; delete [] xx[i++]);
	  delete [] xx;
	  return j;
	}
      else
	{
	  long long **xx = new long long*[c];
	  int i;
	  xx[0] = x;
	  for (i = 1; i < c; xx[i] = xx[i++ - 1] + r);
	  i = ScilabJavaObject::wrapLong(vm, xx, c, r);
	  delete [] xx;
	  return i;
	}
    }
  return -1;
}
#endif

void
unwrapdouble (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapDouble(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwraprowdouble (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapRowDouble(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwrapmatdouble (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapMatDouble(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwrapbyte (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapByte(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwraprowbyte (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapRowByte(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwrapmatbyte (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapMatByte(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwrapshort (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapShort(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwraprowshort (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapRowShort(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwrapmatshort (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapMatShort(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwrapint (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try 
	{
	  ScilabJavaObject2::unwrapInt(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwraprowint (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try 
	{
	  ScilabJavaObject2::unwrapRowInt(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwrapmatint (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try 
	{
	  ScilabJavaObject2::unwrapMatInt(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwrapboolean (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapBoolean(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwraprowboolean (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapRowBoolean(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwrapmatboolean (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapMatBoolean(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwrapstring (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapString(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwraprowstring (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapRowString(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwrapmatstring (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapMatString(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwrapchar (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapChar(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwraprowchar (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapRowChar(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwrapmatchar (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapMatChar(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwrapfloat (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapFloat(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwraprowfloat (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapRowFloat(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwrapmatfloat (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapMatFloat(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwraplong (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapLong(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwraprowlong (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapRowLong(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

void
unwrapmatlong (int id, int pos, char **errmsg)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      try
	{
	  ScilabJavaObject2::unwrapMatLong(vm, id, pos);
	}
      catch (char const *e)
	{
	  *errmsg = strdup(e);
	}
    }
}

int
isunwrappable (int id)
{
  JavaVM *vm = getScilabJavaVM ();
  if (vm)
    {
      return ScilabJavaObject::isUnwrappable(vm, id);
    }

  return -1;
}
