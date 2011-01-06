#include "api_scilab.h"
#include "stack-c.h"
#include "ScilabObjectsk.h"
#include "import_java.h"
#include "MALLOC.h"
#include <stdio.h>

int
sci_wrapinchar (char *fname)
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

  if (type != sci_ints)
    {
      Scierror(999, "%s: Wrong argument type : uint16 expected\n", fname);
      return 0;
    }

  err = getMatrixOfIntegerPrecision(pvApiCtx, addr, &type);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }

  if (type != SCI_UINT16)
    {
      Scierror(999, "%s: Wrong argument type : uint16 expected\n", fname);
      return 0;
    }
  
  int id, row, col;
  unsigned short *data = NULL;
  err = getMatrixOfUnsignedInteger16(pvApiCtx, addr, &row, &col, &data); 
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }

  if (row == 0 || col == 0)
    {
      id = 0;
    }
  else if (row == 1 && col == 1)
    {
      id = wrapSingleChar(data[0]);
    }
  else if (row == 1)
    {
      id = wrapRowChar(data, col);
    }
  else
    {
      id = wrapMatChar(data, row, col);
    }

  if (!createJObj(Rhs + 1, id))
    {
      return 0;
    }

  LhsVar(1) = Rhs + 1;

  return 0;
}
