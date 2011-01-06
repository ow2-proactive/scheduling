#include "api_scilab.h"
#include "stack-c.h"
#include "MALLOC.h"
#include "ScilabObjectsk.h"
#include "import_java.h"
#include <stdio.h>

int
sci_getRep(char *fname)
{
  CheckRhs(1, 1);
  
  SciErr err;
  int tmpvar[2] = {0, 0};
  int *addr = NULL;
  
  err = getVarAddressFromPosition(pvApiCtx, 1, &addr);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }
  
  int idObj = getIdOfArg(addr, fname, tmpvar, 0, 1);

  if (idObj == -1)
    {
      return 0;
    }

  char *errmsg = NULL;
  char* rep = getrepresentation(idObj, &errmsg);
  
  if (errmsg)
    {
      Scierror(999, "%s: An exception has been thrown by Java :\n%s\n", fname, errmsg);
      FREE(errmsg);
      return 0;
    }

  err = createMatrixOfString(pvApiCtx, Rhs + 1, 1, 1, &rep);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }

  LhsVar(1) = Rhs + 1;
  FREE(rep);

  return 0;
}
