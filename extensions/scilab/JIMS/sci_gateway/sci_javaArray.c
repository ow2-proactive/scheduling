#include "api_scilab.h"
#include "stack-c.h"
#include "ScilabObjectsk.h"
#include "import_java.h"
#include "MALLOC.h"

int
sci_javaArray (char *fname)
{
  if (Rhs < 2) 
    {
      Scierror(999, "%s: Wrong number of arguments : more than 2 arguments expected\n", fname);
      return 0;
    }

  SciErr err;
  char *className = getSingleString(1, fname);

  if (!className)
    {
      return 0;
    }

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
      args[i] = getSingleInt(i + 2, fname);
      // If args[i] == -1 then we have a scilab variable which cannot be converted in a Java object.
      if (args[i] == - 1)
	{
	  FREE(args);
	  return 0;
	}
    }

  char *errmsg = NULL;
  int ret = createjavaarray(className, args, Rhs - 1, &errmsg);
  FREE(args);

  if (errmsg)
    {
      Scierror(999, "%s: An exception has been thrown by Java :\n%s\n", fname, errmsg);
      FREE(errmsg);
      return 0;
    }

  if (!createJArray(Rhs + 1, ret)) 
    {
      return 0;
    }

  LhsVar(1) = Rhs + 1;

  return 0;
}
