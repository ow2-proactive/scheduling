/**
  PROJECT NAME:
    PAPI (ProActive Parallel Interface)
  FILE NAME:
    PAPI_sample.c
  DEPENDENCY FILE(S):
    -

  DATE:
    01-28-04
  UPDATE:
    07-29-04
  REVISION NUMBER:*/
#ifndef PAPI_REV_NUMBER
  #define PAPI_REV_NUMBER "1AA-040730-00"
#endif
/*CHECKED:
    Yes

  LANGUAGE:
    C
  COMPILATION LINE:
    mpicc -DPAPI_java -I/usr/local/jdk1.4.0/include -I/usr/local/jdk1.4.0/include/linux PAPI_int.c PAPI_sample.c -o ../bin/libPAPI_sample.so -shared
    mpicc PAPI_int.c PAPI_sample.c  -o ../bin/PAPI_sample
  ENVIRONMENT CONFIGURATION:
    PATH= MPI include path

  DESCRIPTION:
    Accessibility of the MPICH implementation library within a JAVA class
      and to JAVA code from C/MPI code
    Sample program
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

#include "../MPI_C_Interface.h"



/**
  Compilation directives
/**/

EXTERNAL(MPI_C_Interface_java)     /* The current instance of this code is running under supervision of a JAVA if this item is set */

INTERNAL(_RELEASE)
INTERNAL(_DEBUG)
INTERNAL(_MPI_C_Interface_F_callMPI)

#define  _DEBUG  /* Debugging mode */


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

JNIEXPORT void JNICALL MPI_C_Interface_startMPI( JNIEnv * env, jobject jo )
/**
  INPUT PARAMETER(S):
    1- JNI Environment data
    2- Owner object
  OUTPUT PARAMETER(S):
    -
  RETURN VALUE(S):
    -

  ACCESSED VARIABLE(S):
    process
    message
  SIDE EFFECT(S):
    process
    message

  CHECKED:
    Yes

  DESCRIPTION:
    Contain all the operation to do on the C/MPI side
    Main entrance point for JAVA version
  REMARK(S):
    -
/**/
{
  /**
    Local variable declarations and initialisations
  /**/

  int         i;


  /**
    Function body
  /**/

  #ifdef MPI_C_Interface_mpi_all
    if(process.rank.me!=1)
    {
      /* Message sending to the process 1 */
      sprintf( message.text, "Hello World! from the process #%d", process.rank.me );
      message.rank.receiver = 1;
      message.tag           = MESSAGE_TAG;
      message.comm          = MESSAGE_COMM;
      MPI_Send( message.text, strlen( message.text )+1, MPI_CHAR,
                message.rank.receiver, message.tag, message.comm );
    }
    else
    {
      printf( "_ Process #%d [" __FILE__ "/" __FUNCTION__ "] > Start of the work...\n",
                process.rank.me);

      /* Receive loop from all other processes */
      for(message.rank.sender=2;
          message.rank.sender<process.number;
  	      message.rank.sender++)
      {
        CATCH_ERROR( MPI_Recv( message.text, MESSAGE_LENGTH, MPI_CHAR,
                               message.rank.sender, MESSAGE_TAG, MESSAGE_COMM,
		               &message.status ) );
        printf( "_ Process #%d [" __FILE__ "/" __FUNCTION__ "] > Message received: %s\n",
                process.rank.me, message.text );
      }

      printf( "_ Process #%d [" __FILE__ "/" __FUNCTION__ "] > ... The work is over\n",
                process.rank.me);
    }
  #endif

  #ifdef MPI_C_Interface_mpi_all
    if(process.rank.me==1)
    {
      printf( "_ Process #%d [" __FILE__ "/" __FUNCTION__ "] > Receiving data from the JAVA side...\n",
                process.rank.me);
      CATCH_ERROR( MPI_Recv( message.text, 6,MPI_CHAR,
                             0, MPI_C_Interface_Tag, (MPI_Comm) MPI_C_Interface_Comm,
		             &message.status ) );
	printf( "_ Process #%d [" __FILE__ "/" __FUNCTION__ "] > SIZE= %u\n",process.rank.me,
                message.status);

  {
    int     i;
    for( i=0; i<6; i++ )
    {
      printf( "%c", message.text[i] );
    }
    puts( "" );
  }

      printf( "_ Process #%d [" __FILE__ "/" __FUNCTION__ "] > ... Data received\n",
                process.rank.me);
    }
  #endif

    #ifdef MPI_C_Interface_mpi_all
    if(process.rank.me==1)
    {
      strcpy( message.text, "bcdefg" );
      printf( "_ Process #%d [" __FILE__ "/" __FUNCTION__ "] > Sending data from the MPI side...\n",
                process.rank.me);
      CATCH_ERROR( MPI_Send( message.text, 6, MPI_CHAR,
                             0, MPI_C_Interface_Tag, (MPI_Comm) MPI_C_Interface_Comm ) );
  {
    int     i;

    for( i=0; i<6; i++ )
    {
      printf( "%c", message.text[i] );
    }
    puts( "" );
  }

      printf( "_ Process #%d [" __FILE__ "/" __FUNCTION__ "] > ... Data sent\n",
                process.rank.me);
    }
  #endif

  #ifdef MPI_C_Interface_java
  {
    jint p1 = 14;
    jobject q;
    jfieldID q_status;
    puts("MPI_C_Interface_Call BEGIN\n");
    q = MPI_C_Interface_callMPI( env, jo, "MPI_C_Interface_sample", "methodejava", "(I)Ljava/lang/Object;", p1 );
    puts("MPI_C_Interface_Call END\n");

    printf("MPI_C_Interface_Call RETURN : %u\n",
           (*env)->GetStaticIntField( env,
	                              q,
				      (*env)->GetStaticFieldID( env,
				                                (*env)->GetObjectClass( env, q),
								"status",
								"I")) );
  }
  #endif
}


#ifndef MPI_C_Interface_java
main(int argc, char ** argv)
/**
  INPUT PARAMETER(S):
    1- Number of arguments onto the command line
    2- Command line
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
    Main entrance point for MPI alone version
  REMARK(S):
    -
/**/
{
  /**
    Function body
  /**/
  MPI_C_Interface_initMPI( &argc, argv );
  MPI_C_Interface_startMPI( &argc, argv );
  MPI_C_Interface_endMPI( &argc, argv );
}
#endif

/**
  END OF FILE
  WARNING:
    A blank line MUST follow this comment in order to avoid compilation errors!
/**/

