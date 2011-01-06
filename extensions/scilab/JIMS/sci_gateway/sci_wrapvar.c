#include "api_scilab.h"
#include "stack-c.h"
#include "ScilabObjectsk.h"
#include "import_java.h"
#include "MALLOC.h"
#include <stdio.h>

int
sci_wrapvar (char *fname)
{  
  CheckRhs(1, 1);

  SciErr err;
  char *varName = getSingleString(1, fname);
  if (!varName)
    {
      return 0;
    }

  int *addr = NULL;
  err = getVarAddressFromName(pvApiCtx, varName, &addr);
  if (err.iErr)
    {
      FREE(varName);
      printError(&err, 0);
      return 0;
    }

  if (!addr)
    {
      Scierror(999, "%s: No variable named %s on the stack\n", fname, varName);
      FREE(varName);
      return 0;
    }
  
  FREE(varName);
  int tmpvar[] = {0, 0};
  int id = getIdOfArg(addr, fname, tmpvar, 0, 1);
 
  if (id == -1)
    {
      return 0;
    }

  if (!createJObj(Rhs + 1, id)) 
    {
      return 0;
    }

  LhsVar(1) = Rhs + 1;

  return 0;
}
