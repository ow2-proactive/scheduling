#define MAX_INT (1 << 30)

#define MSQ_SIZE	16384
#define MSG_SIZE	8152 /* 8192 */  // pms=40, data=8152, 40+8152=8192bytes
// #define MSG_SIZE 4056 /*4096*/ //pms=40, data=4056, 40+4056=4096bytes
// #define MSG_SIZE 2008 /*2048*/ //pms=40, data=2008, 40+2008=2048bytes
// #define MSG_SIZE 4060 //pms=36, data=4060, 36+4060=4096bytes
// #define MSG_SIZE 2524 //pms=36, data=2524, 36+2524=2560bytes

#define MET_SIZE 128
#define ACCESS_PERM 0666 
#define MAX_NODE 256

/* used as KEY of message queue */
#define C2S_KEY 3026 
#define S2C_KEY	3020
/* used for a potentially second mpi process on same host */
#define C2S02_KEY 3032 
#define S2C02_KEY 3038

#define PROACTIVE_KEY 3044

#define SEM_ID_JAVA 250
#define SEM_ID_MPI  350

#define ANY_SRC -2
#define ANY_TAG -1


/* TAGs used in message's "TAG1" block */
#define MSG_SEND			2
#define MSG_RECV			4
#define MSG_INIT			6
#define MSG_ALLSEND       	8
#define MSG_FINALIZE		10
#define MSG_SEND_PROACTIVE	12


union semun{
	int val;	
	struct semid_ds *buf;		
	unsigned short int *array;
	struct seminfo *__buf;	
};


/* send buffer used in m_s.c Send_Request_Loop() */
typedef struct _msg {	// to be put into common header file "javampi.h"
  long int TAG;
  int TAG1;
  int idjob;
  int count, src, dest, datatype, tag;
  char method[MET_SIZE];
  char data[MSG_SIZE];
} msg_t ;

typedef struct _proactiveRequest {	// to be put into common header file
									// "javampi.h"
	  void *buf;
	  int flag; 
	  int idjob;
	  int count, src, datatype, tag;
} ProActiveMPI_Request;


/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+----- PROACTIVE <-> MPI FUNCTIONS  -+-----+- */
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

int ProActiveSend(void* buf, int count, MPI_Datatype datatype, int dest, char* clazz, char* method, int idjob, ...);
int ProActiveRecv(void* buf, int count, MPI_Datatype datatype, int src, int tag, int idjob);
int ProActiveIRecv(void* buf, int count, MPI_Datatype datatype, int src, int tag, int idjob, ProActiveMPI_Request * request);
int ProActiveTest(ProActiveMPI_Request *r, int* flag);
int ProActiveWait(ProActiveMPI_Request *r);

/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+---- MPI/C <-> MPI/CFUNCTIONS -+-----+----+- */
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

int ProActiveMPI_Init(int rank);
int ProActiveMPI_Send(void* buf, int count, MPI_Datatype datatype, int dest, int tag, int idjob );
int ProActiveMPI_Recv(void* buf, int count, MPI_Datatype datatype, int src, int tag, int idjob); 
int ProActiveMPI_IRecv(void* buf, int count, MPI_Datatype datatype, int src, int tag, int idjob, ProActiveMPI_Request * request); 
int ProActiveMPI_Test(ProActiveMPI_Request *r, int* flag);
int ProActiveMPI_Wait(ProActiveMPI_Request *r);
int ProActiveMPI_AllSend(void * buf, int count, MPI_Datatype datatype, int tag, int idjob);
int ProActiveMPI_Job(int * job);
int ProActiveMPI_Barrier(int job);
int ProActiveMPI_Finalize();


/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+- MPI/F77 <-> MPI/F77 FUNCTIONS -+---+----+- */
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

void proactivempi_init_(int* rank,  int* ierr);
void proactivempi_job_(int* job, int* ierr);
void proactivempi_send_(void * buf, int* cnt, MPI_Datatype* datatype, int* dest, int* tag, int* idjob, int* ierr);
void proactivempi_recv_(void* buf, int* cnt, MPI_Datatype* datatype, int* src, int* tag, int* idjob, int* ierr);
void proactivempi_finalize_(int* ierr);
