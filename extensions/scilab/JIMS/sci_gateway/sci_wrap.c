#include "api_scilab.h"
#include "stack-c.h"
#include "ScilabObjectsk.h"
#include "import_java.h"
#include "MALLOC.h"
#include <stdio.h>

int
sci_wrap (char *fname)
{
  if (Rhs == 0)
    {
      Scierror(999,"%s: Wrong number of input arguments: 1 or more expected\n", fname);
      return 0;
    }

  CheckLhs(Rhs, Rhs);
  
  SciErr err;
  int tmpvar[] = {0, 0};
  int *addr = NULL;
  int i, id;

  for (i = 1; i < Rhs + 1; i++) 
    {
      err = getVarAddressFromPosition(pvApiCtx, i, &addr);
      if (err.iErr)
	{
	  printError(&err, 0);
	  return 0;
	}
      
      id = getIdOfArg(addr, fname, tmpvar, 0, i);
      *tmpvar = 0;
      if (id == - 1)
	{
	  if (i == 1)
	    OverLoad(1);
	  
	  return 0;
	}
      if (!createJObj(Rhs + i, id))
	{
	  return 0;
	}

      LhsVar(i) = Rhs + i;
    }

  return 0;
}
