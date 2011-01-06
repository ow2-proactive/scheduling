#include "api_scilab.h"
#include "stack-c.h"
#include "ScilabObjectsk.h"
#include "import_java.h"
#include "MALLOC.h"

int
sci_unwrap (char *fname)
{
  CheckRhs(1, 1);
  
  SciErr err;
  int *addr = NULL;
  int row, col, type;
  int *id;

  err = getVarAddressFromPosition(pvApiCtx, 1, &addr);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }

  err = getVarType(pvApiCtx, addr, &type);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }
  
  if (type != sci_mlist || !isJObj(addr))
    {
      Scierror(999, "%s: Wrong type for argument 1 : _JObj expected\n", fname);
      return 0;
    }
     
  err = getMatrixOfInteger32InList(pvApiCtx, addr, 2, &row, &col, &id);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }

  if (unwrap(*id, Rhs + 1))
    {
      LhsVar(1) = Rhs + 1;
    }
  else 
    {
      LhsVar(1) = Rhs;
    }

  return 0;
}
