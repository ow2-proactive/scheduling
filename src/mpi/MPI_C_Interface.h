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

#ifdef ProActiveMPI_java
  #include "ProActiveMPI.h"
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

#define MPI_C_Interface_Init_ok_              1  /* MPI was correctly initialized by MPI_C_Interface. Internal use only. Do not change the value */
#define MPI_C_Interface_Tag                6902  /* MPI_C_Interface specific tag. External use */
#define MPI_C_Interface_Comm     MPI_COMM_WORLD  /* MPI_C_Interface specific MPI communicator. Subject to change in future releases */

#ifdef ProActiveMPI_java
  #define DEBUG_MESSAGE_JAVA "Under the supervision of JAVA"
#else
  #define DEBUG_MESSAGE_JAVA "Alone"
#endif /* PAPI_java */


/**
  Structure declarations
/**/

#ifndef ProActiveMPI_java
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
                                                          (please refere to MPI_C_Interface.java to check the validity of prototypes)
/**/

#define MPI_C_Interface_startMPI( obj1, obj2 )                             Java_MPI_C_Interface_startMPI( obj1, obj2)
#define MPI_C_Interface_initMPI( obj1, obj2 )                              Java_MPI_C_Interface_initMPI( obj1, obj2)
#define MPI_C_Interface_endMPI( obj1, obj2 )                               Java_MPI_C_Interface_endMPI( obj1, obj2)
#define MPI_C_Interface_sendToMPI( obj1, obj2, obj3, obj4, obj5 )          Java_MPI_C_Interface_sendToMPI( obj1, obj2, obj3,obj4,obj5)
#define MPI_C_Interface_recvFromMPI( obj1, obj2, obj3, obj4, obj5, obj6 )  Java_MPI_C_Interface_recvFromMPI( obj1, obj2, obj3,obj4,obj5, obj6)
#ifdef _MPI_C_Interface_F_callMPI
  #define MPI_C_Interface_callMPI(obj1, obj2, obj3, obj4, obj5, obj6 )     Java_MPI_C_Interface_callMPI(obj1, obj2, obj3,obj4,obj5, obj6)
#endif /* _MPI_C_Interface_F_callMPI */


/**
  Functions prototypes
/**/

void catch_error( const char *, const char *, int, int );


/**
  END OF FILE
  WARNING:
    A blank line MUST follow this comment in order to avoid compilation errors!
/**/

