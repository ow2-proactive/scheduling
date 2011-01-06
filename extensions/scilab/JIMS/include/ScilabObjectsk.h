#ifndef __SCILABOBJECTS_H__
#define __SCILABOBJECTS_H__
/*--------------------------------------------------------------------------*/
/*--------------------------------------------------------------------------*/

#if !defined(bbyte)
typedef signed char bbyte;
#endif

extern char methodOfConv;

void garbagecollect();
int loadjavaclass(char*, char**);
int createjavaarray(char*, int*, int, char**);
int newinstance(int, int*, int, char**);
int invoke(int, char*, int*, int, char**);
void setfield(int, char*, int, char**);
int getfield(int, char*, char**);
int javacast(int, char*, char**);
char* getrepresentation(int, char**);
void removescilabjavaobject(int);
void getaccessiblemethods(int, int, char**);
void getaccessiblefields(int, int, char**);
int wrapSingleDouble(double);
int wrapRowDouble(double*, int);
int wrapMatDouble(double*, int, int);
int wrapSingleInt(int);
int wrapRowInt(int*, int);
int wrapMatInt(int*, int, int);
int wrapSingleUInt(unsigned int);
int wrapRowUInt(unsigned int*, int);
int wrapMatUInt(unsigned int*, int, int);
int wrapSingleByte(bbyte);
int wrapRowByte(bbyte*, int);
int wrapMatByte(bbyte*, int, int);
int wrapSingleUByte(unsigned char);
int wrapRowUByte(unsigned char*, int);
int wrapMatUByte(unsigned char*, int, int);
int wrapSingleShort(short);
int wrapRowShort(short*, int);
int wrapMatShort(short*, int, int);
int wrapSingleUShort(unsigned short);
int wrapRowUShort(unsigned short*, int);
int wrapMatUShort(unsigned short*, int, int);
int wrapSingleString(char*);
int wrapRowString(char**, int);
int wrapMatString(char**, int, int);
int wrapSingleBoolean(int);
int wrapRowBoolean(int*, int);
int wrapMatBoolean(int*, int, int);
int wrapSingleChar(unsigned short);
int wrapRowChar(unsigned short*, int);
int wrapMatChar(unsigned short*, int, int);
int wrapSingleFloat(double);
int wrapRowFloat(double*, int);
int wrapMatFloat(double*, int, int);

#ifdef __SCILAB_INT64__
int wrapSingleLong(long long);
int wrapRowLong(long long*, int);
int wrapMatLong(long long*, int, int);
#endif

void unwrapdouble(int, int, char**);
void unwraprowdouble(int, int, char**);
void unwrapmatdouble(int, int, char**);
void unwrapbyte(int, int, char**);
void unwraprowbyte(int, int, char**);
void unwrapmatbyte(int, int, char**);
void unwrapshort(int, int, char**);
void unwraprowshort(int, int, char**);
void unwrapmatshort(int, int, char**);
void unwrapint(int, int, char**);
void unwraprowint(int, int, char**);
void unwrapmatint(int, int, char**);
void unwrapboolean(int, int, char**);
void unwraprowboolean(int, int, char**);
void unwrapmatboolean(int, int, char**);
void unwrapstring(int, int, char**);
void unwraprowstring(int, int, char**);
void unwrapmatstring(int, int, char**);
void unwrapchar(int, int, char**);
void unwraprowchar(int, int, char**);
void unwrapmatchar(int, int, char**);
void unwrapfloat(int, int, char**);
void unwraprowfloat(int, int, char**);
void unwrapmatfloat(int, int, char**);
void unwraplong(int, int, char**);
void unwraprowlong(int, int, char**);
void unwrapmatlong(int, int, char**);
int isunwrappable(int);

/*--------------------------------------------------------------------------*/
#endif /* __SCILABOBJECTS_H__ */
