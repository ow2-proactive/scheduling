#include "api_scilab.h"
#include "stack-c.h"
#include "ScilabObjectsk.h"
#include "import_java.h"
#include "MALLOC.h"

/* Function invoke is called with more than 2 arguments : invoke(obj,method,arg1,...,argn)
   - obj is a _JObj mlist so we get his id or can be a String or a number. In this last case, the obj is converted
   in a Java object and the id is got.
   - method is the name of the method.
   - arg1,...,argn are the arguments of the called method, if they're not _JObj mlist, they're converted when it is possible
*/
int
sci_invoke (char *fname)
{
  if (Rhs < 2) 
    {
      Scierror(999, "%s: Wrong number of arguments : more than 2 arguments expected\n", fname);
      return 0;
    }

  SciErr err;
  int *tmpvar = NULL;
  tmpvar = (int*)MALLOC(sizeof(int) * (Rhs - 1));
  if (!tmpvar) 
    {
      Scierror(999,"%s: No more memory.\n", fname);
      return NULL;
    }

  *tmpvar = 0;
  int *addr = NULL;
  
  err = getVarAddressFromPosition(pvApiCtx, 1, &addr);
  if (err.iErr)
    {
      FREE(tmpvar);
      printError(&err, 0);
      return 0;
    }

  int idObj = getIdOfArg(addr, fname, tmpvar, 0, 1);

  if (idObj == -1)
    {
      FREE(tmpvar);
      return 0;
    }

  char *methName = getSingleString(2, fname);

  if (!methName)
    {
      FREE(tmpvar);
      return 0;
    }

  int *args = NULL;
  args = (int*)MALLOC(sizeof(int) * (Rhs - 2));
  if (!args) 
    {
      Scierror(999,"%s: No more memory.\n", fname);
      return NULL;
    }

  int i;
  for (i = 0; i < Rhs - 2; i++) 
    {
      err = getVarAddressFromPosition(pvApiCtx, i + 3, &addr);
      if (err.iErr)
	{
	  FREE(args);
	  FREE(tmpvar);
	  printError(&err, 0);
	  return 0;
	}
      args[i] = getIdOfArg(addr, fname, tmpvar, 0, i + 3);
      // If args[i] == -1 then we have a scilab variable which cannot be converted in a Java object.
      if (args[i] == - 1)
	{
	  FREE(args);
	  FREE(tmpvar);
	  return 0;
	}
    }

  char *errmsg = NULL;
  int ret = invoke(idObj, methName, args, Rhs - 2, &errmsg);
  FREE(args);

  // When a scilab var is found, it's converted into a Java object so we destroy this object because it is not referenced
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
