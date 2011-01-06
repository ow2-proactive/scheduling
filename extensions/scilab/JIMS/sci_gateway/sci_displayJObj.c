#include "api_scilab.h"
#include "stack-c.h"
#include "sciprint.h"
#include "ScilabObjectsk.h"
#include "import_java.h"
#include "MALLOC.h"

int
sci_displayJObj(char *fname)
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

  char *str = getrepresentation(*id, &errmsg);
  
  if (errmsg)
    {
      Scierror(999, "%s: An exception has been thrown by Java :\n%s\n", fname, errmsg);
      FREE(errmsg);
      return 0;
    }

  sciprint("%s\n", str);
  FREE(str);

  return 0;
}
