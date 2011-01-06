#include "api_scilab.h"
#include "stack-c.h"
#include "ScilabObjectsk.h"
#include "import_java.h"
#include "MALLOC.h"

int
sci_loadClass (char *fname)
{
  CheckRhs(1, 1);

  char *className = getSingleString(1, fname);
  
  if (!className)
    {
      return 0;
    }

  char *errmsg = NULL;
  int ret = loadjavaclass(className, &errmsg);

  FREE(className);
  
  if (errmsg)
    {
      Scierror(999, "%s: An exception has been thrown by Java :\n%s\n", fname, errmsg);
      FREE(errmsg);
      return 0;
    }

  if (!createJClass(Rhs + 1, ret)) 
    {
      return 0;
    }

  LhsVar(1) = Rhs + 1;

  return 0;
}
