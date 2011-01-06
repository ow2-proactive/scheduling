#include "api_scilab.h"
#include "stack-c.h"
#include "ScilabObjectsk.h"
#include "MALLOC.h"

int 
createJClass(pos, id)
     int pos;
     int id;
{
  static char* fields[] = {"_JClass", "_id"};
  int* mlistaddr = NULL;
  SciErr err = createMList(pvApiCtx, pos, 2, &mlistaddr);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }
  
  err = createMatrixOfStringInList(pvApiCtx, pos, mlistaddr, 1, 1, 2, fields);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }

  err = createMatrixOfInteger32InList(pvApiCtx, pos, mlistaddr, 2, 1, 1, &id);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }

  return 1;
}

int 
createJArray(pos, id)
     int pos;
     int id;
{
  static char* fields[] = {"_JArray", "_id"};
  int* mlistaddr = NULL;
  SciErr err = createMList(pvApiCtx, pos, 2, &mlistaddr);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }

  err = createMatrixOfStringInList(pvApiCtx, pos, mlistaddr, 1, 1, 2, fields);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }

  err = createMatrixOfInteger32InList(pvApiCtx, pos, mlistaddr, 2, 1, 1, &id);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }

  return 1;
}

int
createJObj(pos, id)
     int pos;
     int id;
{
  static char* fields[] = {"_JObj", "_id"};
  int* mlistaddr = NULL;
  SciErr err = createMList(pvApiCtx, pos, 2, &mlistaddr);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }

  err = createMatrixOfStringInList(pvApiCtx, pos, mlistaddr, 1, 1, 2, fields);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }
  
  err = createMatrixOfInteger32InList(pvApiCtx, pos, mlistaddr, 2, 1, 1, &id);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }
  
  return 1;
}

int 
unwrap(idObj, pos)
     int idObj, pos;
{
  int type = isunwrappable(idObj);
  char *errmsg = NULL;

  switch (type)
    {
    case -1:;
      return 0;
    case 0:;
      unwrapdouble(idObj, pos, &errmsg);
      break;
    case 1:;
      unwraprowdouble(idObj, pos, &errmsg);
      break;
    case 2:;
      unwrapmatdouble(idObj, pos, &errmsg);
      break;
    case 3:;
      unwrapstring(idObj, pos, &errmsg);
      break;
    case 4:;
      unwraprowstring(idObj, pos, &errmsg);
      break;
    case 5:;
      unwrapmatstring(idObj, pos, &errmsg);
      break;
    case 6:;
      unwrapint(idObj, pos, &errmsg);
      break;
    case 7:;
      unwraprowint(idObj, pos, &errmsg);
      break;
    case 8:;
      unwrapmatint(idObj, pos, &errmsg);
      break;
    case 9:;
      unwrapboolean(idObj, pos, &errmsg);
      break;
    case 10:;
      unwraprowboolean(idObj, pos, &errmsg);
      break;
    case 11:;
      unwrapmatboolean(idObj, pos, &errmsg);
      break;
    case 12:;
      unwrapbyte(idObj, pos, &errmsg);
      break;
    case 13:;
      unwraprowbyte(idObj, pos, &errmsg);
      break;
    case 14:;
      unwrapmatbyte(idObj, pos, &errmsg);
      break;
    case 15:;
      unwrapshort(idObj, pos, &errmsg);
      break;
    case 16:;
      unwraprowshort(idObj, pos, &errmsg);
      break;
    case 17:;
      unwrapmatshort(idObj, pos, &errmsg);
      break;
    case 18:;
      unwrapchar(idObj, pos, &errmsg);
      break;
    case 19:;
      unwraprowchar(idObj, pos, &errmsg);
      break;
    case 20:;
      unwrapmatchar(idObj, pos, &errmsg);
      break;
    case 21:;
      unwrapfloat(idObj, pos, &errmsg);
      break;
    case 22:;
      unwraprowfloat(idObj, pos, &errmsg);
      break;
    case 23:;
      unwrapmatfloat(idObj, pos, &errmsg);
      break;
    case 24:;
      unwraplong(idObj, pos, &errmsg);
      break;
    case 25:;
      unwraprowlong(idObj, pos, &errmsg);
      break;
    case 26:;
      unwrapmatlong(idObj, pos, &errmsg);
      break;
    default:;
      return 0;
    }

  if (errmsg)
    {
      Scierror(999,"%s: %s\n", "unwrap", errmsg);
      return 0;
    }

  return 1;
}
