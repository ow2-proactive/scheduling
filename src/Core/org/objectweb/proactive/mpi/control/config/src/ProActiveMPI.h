#ifndef PROACTIVE_MPI_H_
#define PROACTIVE_MPI_H_

#include <mpi.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <sys/sem.h>
#include <unistd.h>
#include <stdio.h>
#include <signal.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <sys/wait.h>
#include <pthread.h>
#include <sched.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <errno.h>
#include <stdarg.h>
#include <sys/stat.h>

#include "CommonInternalApi.h"


//#define MSG_SIZE	8152 /* 8192 */  // pms=40, data=8152, 40+8152=8192bytes
/*
#define IPC_MSG_SIZE 8192
#define PAYLOAD 8152
*/

// #define MSG_SIZE 4056 /*4096*/ //pms=40, data=4056, 40+4056=4096bytes
// #define MSG_SIZE 2008 /*2048*/ //pms=40, data=2008, 40+2008=2048bytes
// #define MSG_SIZE 4060 //pms=36, data=4060, 36+4060=4096bytes
// #define MSG_SIZE 2524 //pms=36, data=2524, 36+2524=2560bytes

/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+----- NF MESSAGE FUNCTIONS ----------+----+-*/
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

int ProActiveMPI_NF(void * buf, int count, MPI_Datatype datatype, int tag);

/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+----- PROACTIVE <-> MPI FUNCTIONS  -+-----+- */
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

//int ProActiveSend(void* buf, int count, MPI_Datatype datatype, int dest, char* clazz, char* method, int idjob, ...);
//int ProActiveRecv(void* buf, int count, MPI_Datatype datatype, int src, int tag, int idjob);
//int ProActiveIRecv(void* buf, int count, MPI_Datatype datatype, int src, int tag, int idjob, ProActiveMPI_Request * request);
//int ProActiveTest(ProActiveMPI_Request *r, int* finished);
//int ProActiveWait(ProActiveMPI_Request *r);

/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+---- MPI/C <-> MPI/CFUNCTIONS -+-----+----+- */
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

int ProActiveMPI_Init(int rank);
int ProActiveMPI_Send(void* buf, int count, MPI_Datatype datatype, int dest, int tag, int idjob );
int ProActiveMPI_Recv(void* buf, int count, MPI_Datatype datatype, int src, int tag, int idjob); 
int ProActiveMPI_IRecv(void* buf, int count, MPI_Datatype datatype, int src, int tag, int idjob, ProActiveMPI_Request * request); 
int ProActiveMPI_Test(ProActiveMPI_Request *r);
int ProActiveMPI_Wait(ProActiveMPI_Request *r);
int ProActiveMPI_AllSend(void * buf, int count, MPI_Datatype datatype, int tag, int idjob);
int ProActiveMPI_Job(int * job, int * nb_process);
int ProActiveMPI_Barrier(int job);
int ProActiveMPI_Finalize();


/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+- MPI/F77 <-> MPI/F77 FUNCTIONS -+---+----+- */
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

void proactivempi_init_(int* rank,  int* ierr);
void proactivempi_job_(int * job, int * nb_process, int* ierr);
void proactivempi_send_(void * buf, int* cnt, MPI_Datatype* datatype, int* dest, int* tag, int* idjob, int* ierr);
void proactivempi_recv_(void* buf, int* cnt, MPI_Datatype* datatype, int* src, int* tag, int* idjob, int* ierr);
/*
void proActivempi_irecv_(void* buf, int* cnt, MPI_Datatype* datatype, int* src, int* tag, int* idjob, int * request, int* ierr);
void proactivempi_test_(int *r, int* flag, int* ierr);
void proactivempi_wait_(int *r, int* ierr);
*/
void proactivempi_finalize_(int* ierr);  
void proactivempi_barrier_(int* job, int* ierr);
void proactivempi_allsend_(void * buf, int* cnt, MPI_Datatype* datatype, int* tag, int* idjob, int*ierr);


#endif /* PROACTIVE_MPI_H_ */
