/**
  PROJECT NAME:
    PAPI (ProActive Parallel Interface)
  FILE NAME:
    PAPI_int.c
  DEPENDENCY FILE(S):
    -

  DATE:
    01-28-04
  UPDATE:
    08-04-04
  REVISION NUMBER:*/
#ifndef PAPI_REV_NUMBER
  #define PAPI_REV_NUMBER "1AA-040804-00"
#endif
/*CHECKED:
    Yes

  LANGUAGE:
    C
  COMPILATION LINE:
    mpicc -DPAPI_java -I/usr/local/jdk1.4.0/include -I/usr/local/jdk1.4.0/include/linux PAPI_int.c -c 
    mpicc PAPI_int.c -c  
  ENVIRONMENT CONFIGURATION:
    PATH= MPI include path

  DESCRIPTION:
    Accessibility of the MPICH implementation library within a JAVA class
      and to JAVA code from C/MPI code
    MAin C code
  REMARK(S):
    -
/**/


/**
  Special header files
/**/

#ifdef MPI_C_Interface_java
  #include <jni.h>
#endif
#include "mpi.h"

#include "MPI_C_Interface.h"


/**
  Compilation directives
/**/

EXTERNAL(MPI_C_Interface_java)     /* The current instance of this code is running under supervision of a JAVA if this item is set */

INTERNAL(_RELEASE)
INTERNAL(_DEBUG)
INTERNAL(_MPI_C_Interface_F_Call)

#define  _DEBUG          /* Debugging mode */
#define   MPI_C_Interface_mpi_init  /* MPI_Init routine activated */
#define   MPI_C_Interface_mpi_all   /* MPI is in use */
#define  _MPI_C_Interface_F_Call    /* MPI_C_Interface_Call function activated */


/**
  Standard header files
/**/

#include <stdio.h>
#include <string.h>
#include <stdarg.h>


/**
  Constant and macro definitions
/**/

#define MESSAGE_TAG    60
#define MESSAGE_COMM   MPI_COMM_WORLD


/**
  Global variable declarations and initialisations
/**/

  Process     process;
  Message     message;


/**
  Functions declarations
/**/

void catch_error( const char * name, const char * function, int line, int error )
/**
  INPUT PARAMETER(S):
    1- File name in which the error occured
    2- Function name in which the error occured
    3- Line number
    4- Error type
  OUTPUT PARAMETER(S):
    -
  RETURN VALUE(S):
    -

  ACCESSED VARIABLE(S):
    -
  SIDE EFFECT(S):
    -

  CHECKED:
    Yes

  DESCRIPTION:
    This function deals with any returned error from called functions
  REMARK(S):
    Used in conjonction with CATCH_ERROR
    CATCH_ERROR is the interface of the catch error function which is intended
      to be used by the program designer
    catch_error is the implementation body
    The latest function should not be used straightforward
/**/
{
  /**
    Function body
  /**/
  if(error)
  {
    printf( "! SYSTEM > The error %d occured on line %u of the function %s within the file %s\n",
            error, line, function, name );
    puts( "! SYSTEM > Warning: consider this error has been "
          "voluntarily catched by the program designer" );
    exit(error);
  }
}


#ifdef MPI_C_Interface_java
JNIEXPORT void JNICALL MPI_C_Interface_sendToMPI( JNIEnv * env, jobject jo, jbyteArray text, jint length, jint receiver )
/**
  INPUT PARAMETER(S):
    1- JNI Environment data
    2- Owner object
    3- Message to send
  OUTPUT PARAMETER(S):
    -
  RETURN VALUE(S):
    -

  ACCESSED VARIABLE(S):
    -
  SIDE EFFECT(S):
    -

  CHECKED:
    Yes

  DESCRIPTION:
    Send data from the JAVA side to the C/MPI side
  REMARK(S):
    -
/**/
{
  /**
    Constant and macro definitions
  /**/


  /**
    Local variable declarations and initialisations
  /**/
  jbyte   * table;


  /**
    Function body
  /**/
  table = (*env)->GetByteArrayElements( env, text, 0 );
  CATCH_ERROR( MPI_Send( table, length, MPI_CHAR, receiver, MPI_C_Interface_Tag, (MPI_Comm) MPI_C_Interface_Comm ) );
  #ifdef _DEBUG
  {
    int     i;
    printf( "_ [" __FILE__ "/" __FUNCTION__ "] > Message sent: " );
    for( i=0; i<length; i++ )
    {
      printf( "%c", table[i] );
    }
    puts( "" );
  }
  #endif /* _DEBUG */
  (*env)->ReleaseByteArrayElements( env, text, table, 0 );

  #ifdef _DEBUG
    /* GETARRAYLENGTH */
    printf("JBYTE =%u\n", sizeof(jbyte));
    printf("ADR JBYTEARRAY =%lx\n", text);
    printf("ADR TABLE =%lx\n", table);
  #endif /* _DEBUG */
}
#endif /* MPI_C_Interface_java */


#ifdef MPI_C_Interface_java
JNIEXPORT void JNICALL MPI_C_Interface_recvFromMPI( JNIEnv * env, jobject jo, jbyteArray text, jint length, jint sender, jint status )
/**
  INPUT PARAMETER(S):
    1- JNI Environment data
    2- Owner object
    3- Received message
  OUTPUT PARAMETER(S):
    -
  RETURN VALUE(S):
    -

  ACCESSED VARIABLE(S):
    -
  SIDE EFFECT(S):
    -

  CHECKED:
    Yes

  DESCRIPTION:
    Receive data from the C/MPI side on the JAVA side
  REMARK(S):
    -
/**/
{
  /**
    Constant and macro definitions
  /**/


  /**
    Local variable declarations and initialisations
  /**/
  int         i;

  Message     message;
  jbyte     * table;
  /* jbyteArray jba; */
  
  
  /**
    Function body
  /**/

  CATCH_ERROR( MPI_Recv( message.text, length, MPI_CHAR, sender, MPI_C_Interface_Tag, (MPI_Comm) MPI_C_Interface_Comm, (MPI_Status *) &status ) );
  #ifdef _DEBUG
  {
    int     i;
    printf( "_ [" __FILE__ "/" __FUNCTION__ " (status=%u)] > Message received: ", status );
    for( i=0; i<length; i++ )
    {
      printf( "%c", message.text[i] );
    }
    puts( "" );
   /* jba = (*env)->NewByteArray(env,  length); */
    table = (*env)->GetByteArrayElements( env, text, 0 );
    for (i = 0; i < length; i++)
    {
      table[i] = message.text[i];
    }
  }
  #endif /* _DEBUG */

  (*env)->SetByteArrayRegion(env, text, 0, length, table);

  #ifdef _DEBUG
    puts("********* TRANSFERT NOCLASS**************");
  #endif /* _DEBUG */

  /* (*env)->ReleaseByteArrayElements( env, text, table, 0 ); */
}
#endif /* MPI_C_Interface_java */


#ifdef MPI_C_Interface_java
#ifdef _MPI_C_Interface_F_callMPI
JNIEXPORT jobject JNICALL MPI_C_Interface_callMPI( JNIEnv * env, jobject jo, char * method_classname, char * method_name, char * method_signature, ... )
/**
  INPUT PARAMETER(S):
    1- JNI Environment data
    2- Owner object
    3- ...
  OUTPUT PARAMETER(S):
    -
  RETURN VALUE(S):
    An object (see JAVA/JNI documentation)

  ACCESSED VARIABLE(S):
    -
  SIDE EFFECT(S):
    -

  CHECKED:
    Yes

  DESCRIPTION:
    Calling of a JAVA method from the C/MPI side
  REMARK(S):
    The 'method_signature' argument which is passed to this function MUST be the last one explicitly declared (see va_start())
/**/
{
  /**
    Constant and macro definitions
  /**/


  /**
    Local variable declarations and initialisations
  /**/
  jclass        method_class;
  jmethodID     method_id;
  jobject       method_return;

  va_list       method_arguments;

  /**
    Function body
  /**/
  va_start( method_arguments, method_signature );

  method_class = (*env)->FindClass( env, method_classname );
  if (!(method_id = (*env)->GetStaticMethodID( env, method_class, method_name, method_signature )))
  {
    printf( "! SYSTEM > The function %s does not exist", method_name );
  }
  else
  {
    method_return = (*env)->CallStaticObjectMethodV( env, method_class, method_id, method_arguments );
    va_end( method_arguments );
    return( method_return );
  }
}
#endif /* _MPI_C_Interface_F_callMPI */
#endif /* MPI_C_Interface_java */


JNIEXPORT int JNICALL MPI_C_Interface_initMPI( JNIEnv * env, jobject jo )
/**
  INPUT PARAMETER(S):
    1- JNI Environment data
    2- Owner object
  OUTPUT PARAMETER(S):
    -
  RETURN VALUE(S):
    MPI_C_Interface_Init_ok_ meaning the MPI environment was correctly setted up.
    (its purpose is to synchronize the setup of MPI with the main JAVA thread which has called it)

  ACCESSED VARIABLE(S):
    -
  SIDE EFFECT(S):
    -

  CHECKED:
    Yes

  DESCRIPTION:
    Initialization of the MPI environment
  REMARK(S):
    -
/**/
{
  /**
    Constant and macro definitions
  /**/
  #define COMMANDLINE_SIZE  3
  #define LINE1 "/net/home/rcoudarc/RELEASE/ProActive/bin/mpi/libMPI_C_Interface_sample\0"
  #define LINE2 "-p4pg\0"
  #define LINE3 "/net/home/rcoudarc/RELEASE/ProActive/src/mpi/example/MPI_C_Interface_sample.pg\0"


  /**
    Local variable declarations and initialisations
  /**/

  char      * commandline = LINE1 LINE2 LINE3;
  char      * commandline_heap;
  char     ** commandline_data; /* MUST BE DYNAMICALLY ALLOCATED! */
  int         commandline_size = COMMANDLINE_SIZE;

  int         i;


  /**
    Function body
  /**/
  commandline_heap = (char *) malloc( 3 + strlen( LINE1 ) + strlen( LINE2 ) + strlen( LINE3 ) );
  memcpy( commandline_heap, commandline, 3 + strlen( LINE1 ) + strlen( LINE2 ) + strlen( LINE3 ) );
  commandline_data    = (char **) malloc( COMMANDLINE_SIZE * sizeof( char * ) );
  commandline_data[0] = commandline_heap;
  commandline_data[1] = commandline_heap+sizeof( LINE1 )-1;
  commandline_data[2] = commandline_heap+sizeof( LINE1 )+sizeof( LINE2 )-2;


  #ifdef _DEBUG
    #ifdef MPI_C_Interface_java
      printf( "_ ******DEMARRAGE " DEBUG_MESSAGE_JAVA " [" __FILE__ "/" __FUNCTION__ "] %s %s %s\n",
              commandline_data[0], commandline_data[1], commandline_data[2] );
    #else
      for( i=0; i<*env; i++ )
      {
        printf( "_ DEMARRAGE " DEBUG_MESSAGE_JAVA " [" __FILE__ "/" __FUNCTION__ "] ARGV(%u) =  %s\n", i, jo[i] );
      }
    #endif /* MPI_C_Interface_java */
  #else
    printf( "_ DEMARRAGE [" __FILE__ "/" __FUNCTION__ "]\n" );
  #endif /* _DEBUG */

  /* Initialisation */
  #ifdef MPI_C_Interface_mpi_init
    #ifdef MPI_C_Interface_java
      MPI_Init( &commandline_size, &commandline_data );
    #else
      MPI_Init( env, &jo );
    #endif /* MPI_C_Interface_java */
    printf( "_ INIT [" __FILE__ "/" __FUNCTION__ "] > MPI_COMM_WORLD = %i\n", MPI_COMM_WORLD );
  #endif /* MPI_C_Interface_mpi_init */

  #ifdef MPI_C_Interface_mpi_all
    MPI_Comm_rank( MPI_COMM_WORLD, &process.rank.me );
    MPI_Comm_size( MPI_COMM_WORLD, &process.number  );
  #endif

  #ifdef MPI_C_Interface_mpi_all
    if(process.rank.me==1)
      printf( "_ Process #%d [" __FILE__ "/" __FUNCTION__ "] > "
              "Initialisation information: REVISION NUMBER= " MPI_C_Interface_REV_NUMBER
	          ", NUMBER=%d\n",
	          process.rank.me, process.number );
  #endif /* MPI_C_Interface_mpi_all */

  return MPI_C_Interface_Init_ok_; /* Sync with the JAVA thread */

}


JNIEXPORT void JNICALL MPI_C_Interface_endMPI( JNIEnv * env, jobject jo )
/**
  INPUT PARAMETER(S):
    1- JNI Environment data
    2- Owner object
  OUTPUT PARAMETER(S):
    -
  RETURN VALUE(S):
    -

  ACCESSED VARIABLE(S):
    -
  SIDE EFFECT(S):
    -

  CHECKED:
    Yes

  DESCRIPTION:
    End of MPI using
  REMARK(S):
    -
/**/
{
  /**
    Function body
  /**/
  #ifdef MPI_C_Interface_mpi_all
    MPI_Finalize();
  #endif /* MPI_C_Interface_mpi_all */
}


/**
  END OF FILE
  WARNING:
    A blank line MUST follow this comment in order to avoid compilation errors!
/**/

