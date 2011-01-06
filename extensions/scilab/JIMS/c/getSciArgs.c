#include "api_scilab.h"
#include "stack-c.h"
#include "MALLOC.h"
#include "ScilabObjectsk.h"

#define WRAP(javatype,data)						\
  if (row == 0 || col == 0)						\
    {									\
      returnId = 0;							\
    }									\
  else if (row == 1 && col == 1)					\
    {									\
      returnId = wrapSingle ## javatype(data[0]);			\
    }									\
  else if (row == 1)							\
    {									\
      returnId = wrapRow ## javatype(data, col);			\
    }									\
  else									\
    {									\
      returnId = wrapMat ## javatype(data, row, col);			\
    }									\
  
static int zero = 0;

int
compareIdToMlistType(id, mlist)
     int *id, *mlist;
{
  int i;
  int *mlist_type;
  getListItemAddress(pvApiCtx, mlist, 1, &mlist_type);
  mlist_type += mlist_type[1] * mlist_type[2] + 4;

  for (i = 0; i < nsiz && mlist_type[i] != id[i] ; i++);

  return i == nsiz;
}

int
isJObj(mlist)
     int *mlist;
{
  static int jobj_id[nsiz];
  static int jarr_id[nsiz];
  static char init = 0;
  if (!init)
    {
      C2F(cvname)(jobj_id, "_JObj", &zero, 5);
      C2F(cvname)(jarr_id, "_JArray", &zero, 7);
      init = 1;
    }

  return compareIdToMlistType(jobj_id, mlist) || compareIdToMlistType(jarr_id, mlist);
}

int
isJClass(mlist)
     int *mlist;
{
  static int jclass_id[nsiz];
  static char init = 0;
  if (!init)
    {
      C2F(cvname)(jclass_id, "_JClass", &zero, 7);
      init = 1;
    }

  return compareIdToMlistType(jclass_id, mlist);
}

char*
getSingleString(pos, fname)
     int pos;
     char *fname;
{
  SciErr err;
  int *addr = NULL;
  int typ = 0, row, col, len;
  char *str = NULL;

  err = getVarAddressFromPosition(pvApiCtx, pos, &addr);
  if (err.iErr)
    {
      printError(&err, 0);
      return NULL;
    }
 
  err = getVarType(pvApiCtx, addr, &typ);
  if (err.iErr)
    {
      printError(&err, 0);
      return NULL;
    }
 
  
  if (typ != sci_strings)
    {
      Scierror(999, "%s: Wrong type for input argument %i : String expected\n", fname, pos);
      return NULL;
    }
  
  err = getVarDimension(pvApiCtx, addr, &row, &col);
  if (err.iErr)
    {
      printError(&err, 0);
      return NULL;
    }
 
  if (row != 1 || col != 1) 
    {
      Scierror(999, "%s: Wrong size for input argument %i : Single string expected\n", fname, pos);
      return NULL;
    }

  err = getMatrixOfString(pvApiCtx, addr, &row, &col, &len, NULL);
  if (err.iErr)
    {
      printError(&err, 0);
      return NULL;
    }
 
  str = (char*)MALLOC(sizeof(char) * (len + 1));
  if (!str) 
    {
      Scierror(999,"%s: No more memory.\n", fname);
      return NULL;
    }

  err = getMatrixOfString(pvApiCtx, addr, &row, &col, &len, &str);
  if (err.iErr)
    {
      printError(&err, 0);
      return NULL;
    }
 
  return str;
}
  
int
getSingleInt(pos, fname)
     int pos;
     char *fname;
{
  SciErr err;
  int *addr = NULL;
  int typ = 0, row, col, prec;
  int *id = NULL;

  err = getVarAddressFromPosition(pvApiCtx, pos, &addr);
  if (err.iErr)
    {
      printError(&err, 0);
      return -1;
    }
  
  err = getVarType(pvApiCtx, addr, &typ);
  if (err.iErr)
    {
      printError(&err, 0);
      return -1;
    }
  
  if (typ != sci_ints)
    {
      Scierror(999, "%s: Wrong type for input argument %i : Integer32 expected\n", fname, pos);
      return -1;
    }
  
  err = getMatrixOfIntegerPrecision(pvApiCtx, addr, &prec);
  if (err.iErr)
    {
      printError(&err, 0);
      return -1;
    }
  
  if (prec != SCI_INT32) 
    {
      Scierror(999, "%s: Wrong type for input argument %i : Integer32 expected\n", fname, pos);
      return -1;
    }

  err = getVarDimension(pvApiCtx, addr, &row, &col);
  if (err.iErr)
    {
      printError(&err, 0);
      return -1;
    }
 
  if (row != 1 || col != 1) 
    {
      Scierror(999, "%s: Wrong size for input argument %i : Single integer32 expected\n", fname, pos);
      return -1;
    }

  err = getMatrixOfInteger32(pvApiCtx, addr, &row, &col, &id);
  if (err.iErr)
    {
      printError(&err, 0);
      return -1;
    }
  
  return *id;
}

char**
getStrings(addr, row, col)
     int *addr, *row, *col;
{
  SciErr err;
  int i, rc;
  int *len = NULL;
  char **mat = NULL;
  
  err = getVarDimension(pvApiCtx, addr, row, col);
  if (err.iErr)
    {
      printError(&err, 0);
      return NULL;
    }
 
  rc = *row * *col;
  len = (int*)MALLOC(sizeof(int) * rc);
  if (!len) 
    {
      Scierror(999,"No more memory.\n");
      return NULL;
    }

  err = getMatrixOfString(pvApiCtx, addr, row, col, len, NULL);
  if (err.iErr)
    {
      printError(&err, 0);
      return NULL;
    } 
  
  mat = (char**)MALLOC(sizeof(char*) * rc);
  if (!mat) 
    {
      Scierror(999,"No more memory.\n");
      return NULL;
    }
  
  for (i = 0; i < rc ; i++)
    {
      mat[i] = NULL;
      mat[i] = (char*)MALLOC(sizeof(char) * (len[i] + 1));
      if (!mat[i]) 
	{
	  Scierror(999,"No more memory.\n");
	  return NULL;
	}
    }

  err = getMatrixOfString(pvApiCtx, addr, row, col, len, mat);
  if (err.iErr)
    {
      printError(&err, 0);
      return NULL;
    }
 
  FREE(len);

  return mat;
}


int
getIdOfArg(addr, fname, tmpvars, isClass, pos)
     int *addr, *tmpvars, pos;
     char *fname, isClass;
{
  SciErr err;
  int typ, row = 0, col = 0, returnId;

  err = getVarType(pvApiCtx, addr, &typ);
  if (err.iErr)
    {
      printError(&err, 0);
      return -1;
    }
 
  if (isClass && typ != sci_mlist)
    {
      SciError(999,"%s: Wrong type for input argument %i : _JClass is expected\n", fname, pos);
      return -1;
    }

  switch (typ)
    {
    case sci_matrix :;
      if (isVarComplex(pvApiCtx, addr))
	{
	  Scierror(999,"%s: Wrong type for input argument %i : Complex are not handled\n", fname, pos);
	  return -1;
	}
      
      double *mat;
      
      err = getMatrixOfDouble(pvApiCtx, addr, &row, &col, &mat);
      if (err.iErr)
	{
	  printError(&err, 0);
	  return -1;
	}
      
      WRAP(Double, mat);
      
      tmpvars[++tmpvars[0]] = returnId;
      
      return returnId;
    case sci_ints :;
      int prec;
      void *ints;

      err = getMatrixOfIntegerPrecision(pvApiCtx, addr, &prec);
      if (err.iErr)
	{
	  printError(&err, 0);
	  return -1;
	}
      
      switch (prec) 
	{
	case SCI_INT8 :;
	  err = getMatrixOfInteger8(pvApiCtx, addr, &row, &col, (char**)(&ints));
	  if (err.iErr)
	    {
	      printError(&err, 0);
	      return -1;
	    }
	  WRAP(Byte, ((bbyte*)ints));
	  tmpvars[++tmpvars[0]] = returnId;
	  return returnId;
	case SCI_UINT8 :; 
	  err = getMatrixOfUnsignedInteger8(pvApiCtx, addr, &row, &col, (unsigned char**)(&ints));
	  if (err.iErr)
	    {
	      printError(&err, 0);
	      return -1;
	    }
	  WRAP(UByte, ((unsigned char*)ints));
	  tmpvars[++tmpvars[0]] = returnId;
	  return returnId;
	case SCI_INT16 :;
	  err = getMatrixOfInteger16(pvApiCtx, addr, &row, &col, (short**)(&ints));
	  if (err.iErr)
	    {
	      printError(&err, 0);
	      return -1;
	    }
	  WRAP(Short, ((short*)ints));
	  tmpvars[++tmpvars[0]] = returnId;
	  return returnId;
	case SCI_UINT16 :; 
	  err = getMatrixOfUnsignedInteger16(pvApiCtx, addr, &row, &col, (unsigned short**)(&ints)); 
	  if (err.iErr)
	    {
	      printError(&err, 0);
	      return -1;
	    }
	  WRAP(UShort, ((unsigned short*)ints));
	  tmpvars[++tmpvars[0]] = returnId;
	  return returnId;
	case SCI_INT32 :;
	  err = getMatrixOfInteger32(pvApiCtx, addr, &row, &col, (int**)(&ints));
	  if (err.iErr)
	    {
	      printError(&err, 0);
	      return -1;
	    }
	  WRAP(Int, ((int*)ints));
	  tmpvars[++tmpvars[0]] = returnId;
	  return returnId;
	case SCI_UINT32 :;
	  err = getMatrixOfUnsignedInteger32(pvApiCtx, addr, &row, &col, (unsigned int**)(&ints));
	  if (err.iErr)
	    {
	      printError(&err, 0);
	      return -1;
	    }
	  WRAP(UInt, ((unsigned int*)ints));
	  tmpvars[++tmpvars[0]] = returnId;
	  return returnId;

#ifdef __SCILAB_INT64__
	case SCI_INT64 :;
	  err = getMatrixOfInteger64(pvApiCtx, addr, &row, &col, (long long**)(&ints));
	  if (err.iErr)
	    {
	      printError(&err, 0);
	      return -1;
	    }
	  WRAP(Long, ((long long*)ints));
	  tmpvars[++tmpvars[0]] = returnId;
	  return returnId;
	case SCI_UINT64 :;
	  err = getMatrixOfUnsignedInteger64(pvApiCtx, addr, &row, &col, (unsigned long long**)(&ints));
	  if (err.iErr)
	    {
	      printError(&err, 0);
	      return -1;
	    }
	  WRAP(Long, ((long long*)ints));
	  tmpvars[++tmpvars[0]] = returnId;
	  return returnId;
#endif
	}
    case sci_strings :;
      char **matS = getStrings(addr, &row, &col);
      if (!matS)
	{
	  return -1;
	}
      
      WRAP(String, matS);
      int i;
      for (i = 0; i < row * col ; i++) {
         FREE(matS[i]);
      }
      FREE(matS);
      tmpvars[++tmpvars[0]] = returnId;
      
      return returnId;
    case sci_boolean :;
      int *matB;
      
      err = getMatrixOfBoolean(pvApiCtx, addr, &row, &col, &matB);
      if (err.iErr)
	{
	  printError(&err, 0);
	  return -1;
	}
      WRAP(Boolean, matB);
      tmpvars[++tmpvars[0]] = returnId;
      
      return returnId;
    case sci_mlist :;
      int *id = 0;
      int jc = isJClass(addr);

      if (isClass)
	{
	  if (jc)
	    {
	      err = getMatrixOfInteger32InList(pvApiCtx, addr, 2, &row, &col, &id);
	      if (err.iErr)
		{
		  printError(&err, 0);
		  return -1;
		}
	      return *id;
	    }
	  else
	    {
	      SciError(999,"%s: Wrong type for input argument %i : _JClass is expected\n", fname, pos);
	      return -1;
	    }
	}
	  
      if (isJObj(addr) || jc)
	{
	  err = getMatrixOfInteger32InList(pvApiCtx, addr, 2, &row, &col, &id);
	  if (err.iErr)
	    {
	      printError(&err, 0);
	      return -1;
	    }
	  return *id;
	}
      else 
	{
	  Scierror(999, "%s: Wrong type for input argument %i : _JClass or _JObj are expected\n", fname, pos);
	  return -1;
	}
      
      break;
    default :; 
      Scierror(999, "%s: Wrong type for input argument %i : Cannot wrap it\n", fname, pos);
      return -1;
    }
}
