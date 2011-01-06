#include "api_scilab.h"
#include "stack-c.h"
#include "ScilabObjectsk.h"
#include "import_java.h"
#include "MALLOC.h"
#include <stdio.h>

int
sci_wrapinfloat (char *fname)
{
  CheckRhs(1, 1);
 
  SciErr err;
  int *addr, type;
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

  if (type != sci_matrix || isVarComplex(pvApiCtx, addr))
    {
      Scierror(999, "%s: Wrong argument type : Double expected\n", fname);
      return 0;
    }

  int id, row, col;
  double *data = NULL;
  getMatrixOfDouble(pvApiCtx, addr, &row, &col, &data); 

  if (row == 0 || col == 0)
    {
      id = 0;
    }
  else if (row == 1 && col == 1)
    {
      id = wrapSingleFloat(data[0]);
    }
  else if (row == 1)
    {
      id = wrapRowFloat(data, col);
    }
  else
    {
      id = wrapMatFloat(data, row, col);
    }

  if (!createJObj(Rhs + 1, id))
    {
      return 0;
    }

  LhsVar(1) = Rhs + 1;

  return 0;
}
