#include "api_scilab.h"
#include "stack-c.h"
#include "ScilabObjectsk.h"
#include "import_java.h"
#include "MALLOC.h"

/* Function invoke is called with more than 2 arguments : invoke(obj,method,varargin)
   - obj is a _JObj mlist so we get his id or can be a String or a number. In this last case, the obj is converted
   in a Java object and the id is got.
   - method is the name of the method.
   - varargin is a list containing the arguments.
*/
int
sci_invoke_lu (char *fname)
{
  CheckRhs(3, 3);
  
  SciErr err;
  int *addr = NULL;
  int typ;

  err = getVarAddressFromPosition(pvApiCtx, 3, &addr);
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
  
  int idObj = getSingleInt(1, fname);

  if (idObj == -1)
    {
      return 0;
    }

  char *methName = getSingleString(2, fname);

  if (!methName)
    {
      return 0;
    }

  int *args = (int*)MALLOC(sizeof(int) * len);
  int *tmpvar = (int*)MALLOC(sizeof(int) * (len + 1));
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
      // If args[i] == -1 then we have a scilab variable which cannot be converted in a Java object.
      if (args[i] == - 1)
	{
	  FREE(args);
	  FREE(tmpvar);
	  return 0;
	}
    }

  char *errmsg = NULL;
  int ret = invoke(idObj, methName, args, len, &errmsg);
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

  if (unwrap(ret, Rhs + 1))
    {
      removescilabjavaobject(ret);	
    }
  else if (!createJObj(Rhs + 1, ret))
    {
      return 0;
    }
  
  LhsVar(1) = Rhs + 1;

  return 0;
}
