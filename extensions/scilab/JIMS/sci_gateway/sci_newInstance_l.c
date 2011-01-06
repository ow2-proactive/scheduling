#include "api_scilab.h"
#include "stack-c.h"
#include "ScilabObjectsk.h"
#include "import_java.h"
#include "MALLOC.h"

int
sci_newInstance_l (char *fname)
{
  CheckRhs(2, 2);
  
  SciErr err;
  int *addr = NULL;
  int typ;

  err = getVarAddressFromPosition(pvApiCtx, 2, &addr);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }
  
  err = getVarType(pvApiCtx, addr, &typ);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }
  
  if (typ != sci_list)
    {
      Scierror(999, "%s: Wrong type for input argument %i : List expected\n", fname, 3);
      return NULL;
    }
  
  int len;
  err = getListItemNumber(pvApiCtx, addr, &len);
  if (err.iErr)
    {
      printError(&err, 0);
      return 0;
    }

  int idClass = getSingleInt(1, fname);

  if (idClass == -1)
    {
      return 0;
    }
  
  int *args = NULL;
  args = (int*)MALLOC(sizeof(int) * len);
  if (!args) 
    {
      Scierror(999,"%s: No more memory.\n", fname);
      return NULL;
    }

  int *tmpvar = NULL;
  tmpvar = (int*)MALLOC(sizeof(int) * (len + 1));
  if (!tmpvar) 
    {
      Scierror(999,"%s: No more memory.\n", fname);
      return NULL;
    }

  *tmpvar = 0;
  int *child = NULL;

  int i;
  for (i = 0; i < len; i++)
    {
      err = getListItemAddress(pvApiCtx, addr, i + 1, &child);
      if (err.iErr)
	{
	  FREE(args);
	  FREE(tmpvar);
	  printError(&err, 0);
	  return 0;
	}
      args[i] = getIdOfArg(child, fname, tmpvar, 0, i + 1);
      if (args[i] == - 1)
	{
	  FREE(args);
	  FREE(tmpvar);
	  return 0;
	}
    }
	 
  char *errmsg = NULL;
  int ret = newinstance(idClass, args, len, &errmsg);
  FREE(args);
  
  for (i = 1; i <= *tmpvar; i++)
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
