/**
  PROJECT NAME:
    PAPI (ProActive Parallel Interface)
  FILE NAME:
    PAPI_int.h
  DEPENDENCY FILE(S):
    -

  DATE:
    01-28-04
  UPDATE:
    07-30-04
  REVISION NUMBER:*/
#ifndef PAPI_REV_NUMBER
  #define PAPI_REV_NUMBER "1AA-040730-00"
#endif
/*CHECKED:
    Yes

  LANGUAGE:
    C
  COMPILATION LINE:
    -
  ENVIRONMENT CONFIGURATION:
    PATH= MPI include path

  DESCRIPTION:
    Accessibility of the MPICH implementation library within a JAVA class
      and to JAVA code from C/MPI code
    Header file 
  REMARK(S):
    -
/**/


/**
  Special header files
/**/

#include "mpi.h"


/**
  User's header files
/**/

#ifdef PAPI_java
  #include "PAPI.h"
#endif


/**
  Special stuff
/**/

#define EXTERNAL(item)  /* Any item declared like this must be defined outside the scope of this file */
#define INTERNAL(item)  /* Any item declared like this is supposed to change the binary output */


/**
  Constant and macro definitions
/**/

                                                         /* Error displaying function */
#define CATCH_ERROR(function)  catch_error( __FILE__, __FUNCTION__, __LINE__, function )

#define MESSAGE_LENGTH           100  /* Maximum size of a message */

#define PAPI_Init_ok_              1  /* MPI was correctly initialized by PAPI. Internal use only. Do not change the value */
#define PAPI_Tag                6902  /* PAPI specific tag. External use */
#define PAPI_Comm     MPI_COMM_WORLD  /* PAPI specific MPI communicator. Subject to change in future releases */

#ifdef PAPI_java
  #define DEBUG_MESSAGE_JAVA "Under the supervision of JAVA"
#else
  #define DEBUG_MESSAGE_JAVA "Alone"
#endif /* PAPI_java */


/**
  Structure declarations
/**/

#ifndef PAPI_java
  typedef  int  JNIEnv;
  typedef  char ** jobject;
  #define JNIEXPORT
  #define JNICALL
#endif

typedef
  struct
  {
    int     number; /* Number of processes */
    struct
    {
      int     me;
    }       rank;
  } Process;

typedef
  struct
  {
    int            tag;
    MPI_Comm       comm;
    char           text[MESSAGE_LENGTH];
    MPI_Status     status; /* Return status for receive functions */
    struct
    {
      int     sender;
      int     receiver;
    }              rank;
  } Message;


/**
  User interface mapping
/**/

/**
  External prototypes                                   Internal prototypes 
    used in C user's code                                 and external prototypes usd in JAVA code (without "Java_")
                                                          (please refere to PAPI.java to check the validity of prototypes)
/**/

#define PAPI_Job( obj1, obj2 )                           Java_PAPI_job( obj1, obj2)
#define PAPI_Init( obj1, obj2 )                          Java_PAPI_init( obj1, obj2)
#define PAPI_Finalize( obj1, obj2 )                      Java_PAPI_finalize( obj1, obj2)
#define PAPI_Send( obj1, obj2, obj3, obj4, obj5 )        Java_PAPI_send( obj1, obj2, obj3,obj4,obj5)
#define PAPI_Recv( obj1, obj2, obj3, obj4, obj5, obj6 )  Java_PAPI_recv( obj1, obj2, obj3,obj4,obj5, obj6)
#ifdef _PAPI_F_Call
  #define PAPI_Call(obj1, obj2, obj3, obj4, obj5, obj6 ) Java_PAPI_call(obj1, obj2, obj3,obj4,obj5, obj6)
#endif /* _PAPI_F_Call */


/**
  Functions prototypes
/**/

void catch_error( const char *, const char *, int, int );


/**
  END OF FILE
  WARNING:
    A blank line MUST follow this comment in order to avoid compilation errors!
/**/

