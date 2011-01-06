#include "api_scilab.h"
#include "stack-c.h"
#include "ScilabObjectsk.h"
#include "import_java.h"
#include "MALLOC.h"

int
sci_getFields(char *fname)
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

  if (type != sci_mlist || (!isJObj(addr) && !isJClass(addr)))
    {
      Scierror(999, "%s: Wrong type for argument 1 : _JObj or _JClass expected\n", fname);
      return 0;
    }

  err = getMatrixOfInteger32InList(pvApiCtx, addr, 2, &row, &col, &id);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }
  
  char *errmsg = NULL;
  getaccessiblefields(*id, Rhs + 1, &errmsg);
  if (errmsg)
    {
      Scierror(999,"%s: %s\n", "getFields", errmsg);
      return 0;
    }

  LhsVar(1) = Rhs + 1;
  
  return 0;
}
