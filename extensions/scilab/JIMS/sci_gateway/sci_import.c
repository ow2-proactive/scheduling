#include "api_scilab.h"
#include "stack-c.h"
#include "ScilabObjectsk.h"
#include "import_java.h"
#include "MALLOC.h"
#include <stdio.h>

static int un = 1, deux = 2;

int
sci_import (char *fname)
{
  CheckRhs(1, 1);

  SciErr err;
  char *className = getSingleString(1, fname);
  
  if (!className)
    {
      return 0;
    }
  
  char *name = strrchr(className, '.');
  
  if (!name)
    {
      name = className;
    }
  else if (name[1] == '\0')
    {
      Scierror(999, "%s: The class name cannot end with a dot\n", fname);
      FREE(className);
      return 0;
    }
  else
    {
      name++;
    }
  
  int *addr = NULL;
  getVarAddressFromName(pvApiCtx, name, &addr);
  if (addr)
    {
      Scierror(999, "%s: A variable named %s already exists on the stack\n", fname, name);
      FREE(className);
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

  char *def = NULL;
  def = (char*)MALLOC(sizeof(char) * (strlen(name) + 12 + 1));
  if (!def) 
    {
      Scierror(999,"%s: No more memory.\n", fname);
      return NULL;
    }
  
  sprintf(def, "y=%s(varargin)", name);
  err = createMatrixOfString(pvApiCtx, 1, 1, 1, &def);
  if (err.iErr)
    {
      FREE(def);
      printError(&err, 0);
      return 0;
    }

  char *code = NULL;
  code = (char*)MALLOC(sizeof(char) * (16 + 46 + 1));
  if (!code) 
    {
      Scierror(999,"%s: No more memory.\n", fname);
      return NULL;
    }

  sprintf(code, "y=wrapInMlist(newInstance_l(int32(%i),varargin))", ret);
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
