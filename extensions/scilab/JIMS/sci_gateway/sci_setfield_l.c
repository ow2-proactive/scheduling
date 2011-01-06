#include "api_scilab.h"
#include "stack-c.h"
#include "ScilabObjectsk.h"
#include "import_java.h"
#include "MALLOC.h"

int
sci_setfield_l (char *fname)
{
  CheckRhs(3, 3);
  
  SciErr err;
  int tmpvar[2] = {0, 0};
  int idObj = getSingleInt(1, fname);

  if (idObj == -1)
    {
      return 0;
    }

  char *fieldName = getSingleString(2, fname);

  if (!fieldName)
    {
      return 0;
    }
  
  int *addr = NULL;
  err = getVarAddressFromPosition(pvApiCtx, 3, &addr);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }

  int arg = getIdOfArg(addr, fname, tmpvar, 0, 3);
  
  if (arg == - 1)
    {
      return 0;
    }

  char *errmsg = NULL;
  setfield(idObj, fieldName, arg, &errmsg);
  
  int i;
  for (i = 1; i <= tmpvar[0]; i++)
    {
      removescilabjavaobject(tmpvar[i]);
    }
  
  if (errmsg)
    {
      Scierror(999, "%s: An exception has been thrown by Java :\n%s\n", fname, errmsg);
      FREE(errmsg);
      return 0;
    }

  LhsVar(1) = 0;

  return 0;
}
