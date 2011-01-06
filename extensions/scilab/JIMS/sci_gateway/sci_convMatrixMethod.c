#include "api_scilab.h"
#include "stack-c.h"
#include "import_java.h"
#include "MALLOC.h"
#include <stdio.h>

char methodOfConv = 0;

int
sci_convMatrixMethod (char *fname)
{
  CheckRhs(1, 1);

  char *method = getSingleString(1, fname);
  if (!method)
    return 0;

  int rc = strcmp(method, "rc"), cr = strcmp(method, "cr");
  if (strlen(method) != 2 || (rc && cr))
    {
      Scierror(999, "%s: The argument must \'rc\' or \'cr\'\n", fname);
      FREE(method);
      return 0;
    }
  
  if (!cr)
    {
      methodOfConv = 0;
    }
  else 
    {
      methodOfConv = 1;
    }

  FREE(method);

  LhsVar(1) = 0;

  return 0;
}
