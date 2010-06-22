#include "stack-c.h"
/******************************************
 * SCILAB function : initEmbedded, fin = 1
 ******************************************/

int sci_initEmbedded(char *fname)
{
 int err=0;
 CheckRhs(0,0);
 CheckLhs(1,1);
 /* cross variable size checking */
 C2F(cinitEmbedded)(&err);
 if (err >  0) {
  Scierror(999,"%s: Internal Error \n",fname);
  return 0;
 };
 LhsVar(1)=0;
 ;return 0;
}
/******************************************
 * SCILAB function : connect, fin = 2
 ******************************************/

int sci_connect(char *fname)
{
 int m1,n1,l1,m2,n2,l2,m3,n3,l3,err=0;
 CheckRhs(3,3);
 CheckLhs(1,1);
 /*  checking variable url */
 GetRhsVar(1,"c",&m1,&n1,&l1);
 /*  checking variable login */
 GetRhsVar(2,"c",&m2,&n2,&l2);
 /*  checking variable passwd */
 GetRhsVar(3,"c",&m3,&n3,&l3);
 /* cross variable size checking */
 C2F(cconnect)(cstk(l1),cstk(l2),cstk(l3),&m1,&m2,&m3,&err);
 if (err >  0) {
  Scierror(999,"%s: Internal Error \n",fname);
  return 0;
 };
 LhsVar(1)=0;
 ;return 0;
}
/******************************************
 * SCILAB function : sciSolve, fin = 3
 ******************************************/

int sci_sciSolve(char *fname)
{
 int m1,n1,m2,n2,l2,m3,n3,l3,m4,n4,l4,m5,n5,l5,m6,n6,l6,nsmm7,nsnn7,err=0;
 char  **Str1,**Str7;
 CheckRhs(6,6);
 CheckLhs(1,1);
 /*  checking variable inputscripts */
 GetRhsVar(1,"S",&m1,&n1,&Str1);
 CheckOneDim(1,1,m1,1);
 /*  checking variable functionName */
 GetRhsVar(2,"c",&m2,&n2,&l2);
 /*  checking variable functionsDefinition */
 GetRhsVar(3,"c",&m3,&n3,&l3);
 /*  checking variable mainscript */
 GetRhsVar(4,"c",&m4,&n4,&l4);
 /*  checking variable selectScript */
 GetRhsVar(5,"c",&m5,&n5,&l5);
 /*  checking variable debug */
 GetRhsVar(6,"i",&m6,&n6,&l6);
 CheckScalar(6,m6,n6);
 /* cross variable size checking */
 C2F(csciSolve)(&Str1,cstk(l2),cstk(l3),cstk(l4),cstk(l5),istk(l6),&Str7,&n1,&m2,&m3,&m4,&m5,&err);
 if (err >  0) {
  Scierror(999,"%s: Internal Error \n",fname);
  return 0;
 };
 LhsVar(1)= 7;
 CreateVarFromPtr(7, "S",&m1,&n1,Str7);
 FreeRhsSVar(Str7);
 return 0;
}
