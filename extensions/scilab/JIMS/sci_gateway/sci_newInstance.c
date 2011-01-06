#include "api_scilab.h"
#include "stack-c.h"
#include "ScilabObjectsk.h"
#include "import_java.h"
#include "MALLOC.h"

int
sci_newInstance (char *fname)
{
  if (Rhs == 0)
    {
      Scierror(999, "%s: Wrong number of arguments : more than 1 argument expected\n", fname);
      return 0;
    }

  SciErr err;
  int *addr = NULL;
  err = getVarAddressFromPosition(pvApiCtx, 1, &addr);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }
  
  int idClass = getIdOfArg(addr, fname, NULL, 1, 1);
  if (idClass == -1)
    {
      return 0;
    }
 
  int *tmpvar = NULL;
  tmpvar = (int*)MALLOC(sizeof(int) * Rhs);
  if (!tmpvar) 
    {
      Scierror(999,"%s: No more memory.\n", fname);
      return NULL;
    }

  *tmpvar = 0;
  int *args = NULL;
  args = (int*)MALLOC(sizeof(int) * (Rhs - 1));
  if (!args) 
    {
      Scierror(999,"%s: No more memory.\n", fname);
      return NULL;
    }

  int i;
  for (i = 0; i < Rhs - 1; i++)
    {
      err = getVarAddressFromPosition(pvApiCtx, i + 2, &addr);
      if (err.iErr)
	{
	  FREE(tmpvar);
	  FREE(args);
	  printError(&err, 0);
	  return 0;
	}
      args[i] = getIdOfArg(addr, fname, tmpvar, 0, i + 2);
      if (args[i] == -1)
	{
	  FREE(tmpvar);
	  FREE(args);
	  return 0;
	}
    }
	 
  char *errmsg = NULL;
  int ret = newinstance(idClass, args, Rhs - 1, &errmsg);
  FREE(args);
  
  for (i = 1; i <= tmpvar[0]; i++)
    {
      removescilabjavaobject(tmpvar[i]);
    }
  FREE(tmpvar);
  
  if (errmsg)
    {
      Scierror(999, "%s: An exception has been thrown by Java :\n%s\n", fname, errmsg);
      FREE(errmsg);
      return 0;
    }

  if (!createJObj(Rhs + 1, ret))
    {
      return 0;
    }

  LhsVar(1) = Rhs + 1;

  return 0;
}
