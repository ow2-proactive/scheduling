#include "api_scilab.h"
#include "stack-c.h"
#include "ScilabObjectsk.h"
#include "import_java.h"
#include "MALLOC.h"
#include <stdio.h>

static int un = 1, deux = 2;

int
sci_javadeff (char *fname)
{
  CheckRhs(3, 3);

  SciErr err;
  char *className = getSingleString(1, fname);
  
  if (!className)
    {
      return 0;
    }

  char *methName = getSingleString(2, fname);
  
  if (!methName)
    {
      FREE(className);
      return 0;
    }

  char *errmsg = NULL;
  int ret = loadjavaclass(className, &errmsg);

  if (errmsg)
    {
      Scierror(999, "%s: An exception has been thrown by Java :\n%s\n", fname, errmsg);
      FREE(className);
      FREE(errmsg);
      FREE(methName);
      return 0;
    }

  FREE(className);
  
  char *funName = getSingleString(3, fname);

  if (!funName)
    {
      FREE(methName);
      return 0;
    }
  
  char *def = NULL;
  def = (char*)MALLOC(sizeof(char) * (strlen(funName) + 12 + 1));
  if (!def) 
    {
      Scierror(999,"%s: No more memory.\n", fname);
      return 0;
    }
  
  sprintf(def, "y=%s(varargin)", funName);
  err = createMatrixOfString(pvApiCtx, 1, 1, 1, &def);
  if (err.iErr)
    {
      FREE(def);
      printError(&err, 0);
      return 0;
    }

  char *code = NULL;
  code = (char*)MALLOC(sizeof(char) * (16 + strlen(methName) + 47 + 1));
  if (!code) 
    {
      Scierror(999,"%s: No more memory.\n", fname);
      return 0;
    }

  sprintf(code, "y=wrapInMlist_u(invoke_lu(int32(%i),\"%s\",varargin))", ret, methName);
  err = createMatrixOfString(pvApiCtx, 2, 1, 1, &code);
  if (err.iErr)
    {
      FREE(def);
      FREE(code);
      printError(&err, 0);
      return 0;
    }
  
  SciString(&un, "deff", &un, &deux);

  FREE(def);
  FREE(code);
  
  LhsVar(1) = 0;

  return 0;
}
