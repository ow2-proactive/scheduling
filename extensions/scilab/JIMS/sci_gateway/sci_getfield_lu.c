#include "api_scilab.h"
#include "stack-c.h"
#include "ScilabObjectsk.h"
#include "import_java.h"
#include "MALLOC.h"

int
sci_getfield_lu (char *fname)
{
  CheckRhs(2, 2);

  int idObj = getSingleInt(1, fname);

  if (idObj == -1)
    {
      return 0;
    }

  char *fieldName = getSingleString(2, fname);

  if (!fieldName)
    {
      return 0;
    }

  char *errmsg = NULL;
  int ret = getfield(idObj, fieldName, &errmsg);
  FREE(fieldName);
  
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
