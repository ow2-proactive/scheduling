/*
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 * 
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 * 
 * ################################################################
 */
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
#include <mpi.h>
#include "ProActiveMPI.h"
#include <errno.h>
#include <stdarg.h>

#define DEBUG 0	// most verbose debug info
// if debug is on please define the path variable below
// in the MPI_Init function
#define BIGINT 100000
#define MAX_NOM 32
int INTERVAL = 5 ;

char * path; 

char *daemon_address, *jobmanager, *myhostname;

int C2S_Q_ID, S2C_Q_ID;
int sem_set_id_mpi;            // semaphore set ID.
int myJob=-1;
int myRank=-1;
int TAG_S_KEY;
int TAG_R_KEY;
FILE* mslog;

void msg_stat(int msgid, struct msqid_ds * msg_info);
int openlog(char *path, int rank);
// semaphore functions
void sem_unlock(int sem_set_id);
void sem_lock(int sem_set_id);




/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+-----+- MPI <-> MPI FUNCTIONS -+----+-----+- */
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

/*
 * ProActiveMPI_Init
 */
int ProActiveMPI_Init(int rank){
	int error;
	msg_t send_msg_buf, recv_msg_buf ;
	int pms;
	struct msqid_ds bufstat;
    // keep rank of this process
	myRank=rank;
	if (DEBUG){  
		path = (char*) malloc(56);
		strcpy(path, "/tmp");
		if (openlog(path, rank) < 0){
			printf("ERROR WHILE OPENING FILE PATH= %s \n", path); 
			perror("[ProActiveMPI_Init] openlog");  exit(1);}
		fprintf(mslog, "Initializing queues \n");
	}
	// get the mpi semaphore
	sem_set_id_mpi = semget(SEM_ID_MPI, 1, 0600);
	if  (sem_set_id_mpi == -1){
		perror("[ProActiveMPI_Init] semget");
		exit(1);
	}
	if (DEBUG){
		fprintf(mslog, "Block Semaphore  \n");
	}
	// first process lock the semaphore
	sem_lock(sem_set_id_mpi);
    // accessing exclusively the ClientToServer queue
	if ((C2S_Q_ID = msgget(C2S_KEY, ACCESS_PERM)) == -1) {
		perror("[ProActiveMPI_Init] msgget 1 ");
		if (DEBUG){
			fprintf(mslog, "Cannot open sending queue: %d   \n",C2S_Q_ID);
		}
		return -1;
	}
	// the queue successfully opened
	else {
		// update TAG_KEY
		TAG_S_KEY=C2S_KEY;
		
		// check the pid of the last process which have accessed to the queue
		// if (pid <> 0) open this process is the second one
		msg_stat(C2S_Q_ID, &bufstat);
		if (bufstat.msg_lspid != 0){
			// acess the second message queue
			if ((C2S_Q_ID = msgget(C2S02_KEY,  ACCESS_PERM)) == -1) {
				perror("[ProActiveMPI_Init] msgget C2S_02");
				if (DEBUG){
					fprintf(mslog, "Cannot open the second sending queue: %d   \n",C2S_Q_ID);
				}
				return -1;
			}
			else{
				// update TAG_KEY
				TAG_S_KEY=C2S02_KEY;
				if (DEBUG){ 
					fprintf(mslog, "Second Sending Queue %d successfully opened \n ",C2S_Q_ID); }
			}
		}
		if (DEBUG){ 
			fprintf(mslog, "Sending Queue %d successfully opened \n ",C2S_Q_ID); }
	}
	// accessing exclusively the ServerToClient queue
	if ((S2C_Q_ID = msgget(S2C_KEY,  ACCESS_PERM)) == -1) {
		perror("[ProActiveMPI_Init] mssget S2C_01  ");
		if (DEBUG){
			fprintf(mslog, "Cannot open receiving queue: %d   \n",S2C_Q_ID);
		}
		return -1;
	}
	else {
		TAG_R_KEY=S2C_KEY;
		// check the pid of the last process which have accessed to the queue
		// if (pid <> 0) open the second queue
		if (bufstat.msg_lspid != 0){
			if ((S2C_Q_ID = msgget(S2C02_KEY,  ACCESS_PERM)) == -1) {
				perror("[ProActiveMPI_Init] msgget S2C_02 ");
				if (DEBUG){
					fprintf(mslog, "Cannot open the second recving queue: %d   \n",S2C_Q_ID);
				}
				return -1;
			}
			else{
				TAG_R_KEY=S2C02_KEY;
				if (DEBUG){ 
					fprintf(mslog, "Second Recving Queue %d successfully opened \n ",S2C_Q_ID); }
			}
		}
		if (DEBUG){
			fprintf(mslog, "Receivind Queue %d successfully opened \n ",S2C_Q_ID); }
	}
	// unlock the semaphore
	sem_unlock(sem_set_id_mpi);
	if (DEBUG){
		fprintf(mslog, "UnBlock Semaphore  \n");
	}
	send_msg_buf.TAG1 = MSG_INIT;
	send_msg_buf.TAG = TAG_S_KEY;
	send_msg_buf.src = rank;
// strcpy(send_msg_buf.data, "");
	pms= sizeof(msg_t) - sizeof(send_msg_buf.TAG) - sizeof(send_msg_buf.data);
	// send the rank of this mpi process
	error = msgsnd(C2S_Q_ID, &send_msg_buf, pms+1, 0);
	if (error < 0) {
		if (DEBUG) {fprintf(mslog, "!!! ERROR: msgsnd error\n");}
		perror("ERROR");
		return -1; }
	if (DEBUG){
		fprintf(mslog, "Waiting for job number in recv queue \n "); }
	// wait for the job number
	error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, 0);
	// if an error occured during receive call check if its an interrupted
	// System call and so retry to receive
	while (error < 0){
		strerror(errno);
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
		if (errno == EINTR){
			if (DEBUG) { fprintf(mslog, "!!! ERRNO = EINTR, \n");}
			error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, 0);
		}
		else{
			perror("ERROR");
			return -1; 
		}
	}
	// update the job field of this mpi process
	myJob = recv_msg_buf.idjob;
	if (DEBUG) {fflush(mslog);}
	return 0;
}

/*
 * ProActiveMPI_Job
 */
int ProActiveMPI_Job(int * job){
	*job=myJob;
	return 0;
}


/*
 * ProActiveMPI_Send
 */
int ProActiveMPI_Send(void * buf, int count, MPI_Datatype datatype, int dest, int tag, int idjob)
{ 
	msg_t send_msg_buf;
	int error;
	int pms;
	int length;
	send_msg_buf.TAG1 = MSG_SEND;
	send_msg_buf.count = count;
	send_msg_buf.src = myRank ;
	send_msg_buf.dest = dest;
	send_msg_buf.datatype = datatype;
	send_msg_buf.tag = tag;
	send_msg_buf.TAG = TAG_S_KEY;
	send_msg_buf.idjob = idjob;
	
	switch (datatype){
	case  MPI_CHAR:  case MPI_UNSIGNED_CHAR: case MPI_BYTE:
		if (DEBUG) {
			fprintf(mslog, "MPI_CHAR Datatype found  \n");}
		length = sizeof(char) * count; 
		break;
	case  MPI_INT:  case MPI_UNSIGNED:
		if (DEBUG) {
			fprintf(mslog, "MPI_INT Datatype found  \n");}
		length = sizeof(int) * count;
		break;
	case MPI_SHORT: case MPI_UNSIGNED_SHORT:
		if (DEBUG) {
			fprintf(mslog, "MPI_SHORT Datatype found  \n");}
		length = sizeof(short) * count;
		break; 
	case MPI_LONG: case MPI_UNSIGNED_LONG:
		if (DEBUG) {
			fprintf(mslog, "MPI_LONG Datatype found  \n");}
		length = sizeof(long) * count;
		break; 
	case MPI_FLOAT:
		if (DEBUG) {
			fprintf(mslog, "MPI_FLOAT Datatype found  \n");}
		length = sizeof(float) * count;
		break; 
	case MPI_DOUBLE:
		if (DEBUG) {
			fprintf(mslog, "MPI_DOUBLE Datatype found  \n");}
		length = sizeof(double) * count;
		break; 	
	case MPI_LONG_DOUBLE:
		if (DEBUG) {
			fprintf(mslog, "MPI_LONG_DOUBLE Datatype found  \n");}
		length = sizeof(long double) * count;
		break; 
	case MPI_LONG_LONG:
		if (DEBUG) {
			fprintf(mslog, "MPI_LONG_LONG Datatype found  \n");}
		length = sizeof(long long) * count;
		break; 
	default:
		if (DEBUG) {fprintf(mslog, "!!! BAD DATATYPE \n");}
	return -1;
	}
	memcpy(send_msg_buf.data, buf, length);
	pms= sizeof(msg_t) - sizeof(send_msg_buf.TAG) - sizeof(send_msg_buf.data);
	error = msgsnd(C2S_Q_ID, &send_msg_buf, pms+length, 0);
	if (error < 0) {
		if (DEBUG) {fprintf(mslog, "!!! ERROR: msgsnd error\n");}
		perror("ERROR"); 
		return -1;  }
	if (DEBUG) {fflush(mslog);}
	return 0;
}

/*
 * ProActiveMPI_Recv
 */
int ProActiveMPI_Recv(void* buf, int count, MPI_Datatype datatype, int src, int tag, int idjob){
	msg_t recv_msg_buf;
	int error = -1;
	int pms;
	int length;

	pms= sizeof(msg_t) - sizeof(recv_msg_buf.TAG) - sizeof(recv_msg_buf.data);
	error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, 0);
	// if an error occured during receive call check if its an interrupted
	// System call and so retry to receive
	while (error < 0){
		strerror(errno);
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
		
		if (errno == EINTR){
			if (DEBUG) { fprintf(mslog, "!!! ERRNO = EINTR, \n");}
			error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, 0);
		}
		else{
			perror("ERROR");
			return -1; 
		}
	}
	if (DEBUG) {fflush(mslog);}
	if (recv_msg_buf.idjob != idjob){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER idjob \n");}
		return -1;
	}
	else if ((src != MPI_ANY_SOURCE) && (recv_msg_buf.src != src)){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER src \n");}
		return -1;
	}
	else if ((tag != MPI_ANY_TAG) && (recv_msg_buf.tag != tag)) {
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER tag \n");}
		return -1;
	} 
	else if (recv_msg_buf.datatype != datatype){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER datatype \n");}
		return -1;
	} 
	else{
		switch (datatype){
		case  MPI_CHAR:  case MPI_UNSIGNED_CHAR: case MPI_BYTE:
			if (DEBUG) {
				fprintf(mslog, "MPI_CHAR Datatype found  \n");}
			length = sizeof(char) * count; 
			break;
		case  MPI_INT:  case MPI_UNSIGNED:
			if (DEBUG) {
				fprintf(mslog, "MPI_INT Datatype found  \n");}
			length = sizeof(int) * count;
			break;
		case MPI_SHORT: case MPI_UNSIGNED_SHORT:
			if (DEBUG) {
				fprintf(mslog, "MPI_SHORT Datatype found  \n");}
			length = sizeof(short) * count;
			break; 
		case MPI_LONG: case MPI_UNSIGNED_LONG:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG Datatype found  \n");}
			length = sizeof(long) * count;
			break; 
		case MPI_FLOAT:
			if (DEBUG) {
				fprintf(mslog, "MPI_FLOAT Datatype found  \n");}
			length = sizeof(float) * count;
			break; 
		case MPI_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_DOUBLE Datatype found  \n");}
			length = sizeof(double) * count;
			break; 
			
		case MPI_LONG_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG_DOUBLE Datatype found  \n");}
			length = sizeof(long double) * count;
			break; 
		case MPI_LONG_LONG:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG_LONG Datatype found  \n");}
			length = sizeof(long long) * count;
			break; 
			
		default:
			if (DEBUG) {fprintf(mslog, "!!! BAD DATATYPE \n");}
		return -1;
		}
		memcpy(buf, recv_msg_buf.data, length);
	}
	
	return 0;
	
}

/*
 * ProActiveMPI_IRecv
 */
int ProActiveMPI_IRecv(void* buf, int count, MPI_Datatype datatype, int src, int tag, int idjob, ProActiveMPI_Request *r){
	msg_t recv_msg_buf;
	int error = -1;
	int pms;
	int length;
	pms= sizeof(msg_t) - sizeof(recv_msg_buf.TAG) - sizeof(recv_msg_buf.data);
	error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, IPC_NOWAIT);
	while (error < 0){
		strerror(errno);
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
		if (errno == EINTR){
			if (DEBUG) { fprintf(mslog, "!!! ERRNO = EINTR, \n");}
			error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, IPC_NOWAIT);
		}
		// no message in the queue
		else if (errno == ENOMSG){
			if (DEBUG) {
				fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
			r->buf = buf; // keep buf address in structure to update it later
			(*r).flag = 0; // nothing recv yet
			// keep parameters for further recv
			(*r).count = count;
			(*r).datatype = datatype;
			(*r).src = src;
			(*r).tag = tag;
			(*r).idjob = idjob;
			return 0;
		}
		else{
			perror("[ProActiveMPI_IRecv]!!! ERROR");
			return -1; 
		}
	}
	
	if (DEBUG) {fflush(mslog);}
	// filter
	if (recv_msg_buf.idjob != idjob){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER idjob \n");}
		return -1;
	}
	else if ((src != MPI_ANY_SOURCE) && (recv_msg_buf.src != src)){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER src \n");}
		return -1;
	}
	else if (recv_msg_buf.tag != tag) {
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER tag \n");}
		return -1;
	} 
	else if (recv_msg_buf.datatype != datatype){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER datatype \n");}
		return -1;
	} 
	else{
		switch (datatype){
		case  MPI_CHAR:  case MPI_UNSIGNED_CHAR: case MPI_BYTE:
			if (DEBUG) {
				fprintf(mslog, "MPI_CHAR Datatype found  \n");}
			length = sizeof(char) * count; 
			break;
		case  MPI_INT:  case MPI_UNSIGNED:
			if (DEBUG) {
				fprintf(mslog, "MPI_INT Datatype found  \n");}
			length = sizeof(int) * count;
			break;
		case MPI_SHORT: case MPI_UNSIGNED_SHORT:
			if (DEBUG) {
				fprintf(mslog, "MPI_SHORT Datatype found  \n");}
			length = sizeof(short) * count;
			break; 
		case MPI_LONG: case MPI_UNSIGNED_LONG:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG Datatype found  \n");}
			length = sizeof(long) * count;
			break; 
		case MPI_FLOAT:
			if (DEBUG) {
				fprintf(mslog, "MPI_FLOAT Datatype found  \n");}
			length = sizeof(float) * count;
			break; 
		case MPI_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_DOUBLE Datatype found  \n");}
			length = sizeof(double) * count;
			break; 
			
		case MPI_LONG_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG_DOUBLE Datatype found  \n");}
			length = sizeof(long double) * count;
			break; 
		case MPI_LONG_LONG:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG_LONG Datatype found  \n");}
			length = sizeof(long long) * count;
			break; 
						
		default:
			if (DEBUG) {fprintf(mslog, "!!! BAD DATATYPE \n");}
		return -1;
		}
		memcpy(buf, recv_msg_buf.data, length);	
	}
	return 0;
}

/*
 * ProActiveMPI_Wait
 */
int ProActiveMPI_Wait(ProActiveMPI_Request *r){
	msg_t recv_msg_buf;
	int error = -1;
	int pms;
	int length;
	
	int idjob = (*r).idjob;
	int tag = (*r).tag;
	int count = (*r).count;
	int datatype = (*r).datatype;
	int src = (*r).src;
	
	pms= sizeof(msg_t) - sizeof(recv_msg_buf.TAG) - sizeof(recv_msg_buf.data);
	
	error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, 0);
	while (error < 0){
		strerror(errno);
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
		
		if (errno == EINTR){
			if (DEBUG) { fprintf(mslog, "!!! ERRNO = EINTR, \n");}
			error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, 0);
		}
		// no message in the queue
		else{
			perror("ERROR");
			return -1; 
		}
	}
	
	if (DEBUG) {fflush(mslog);}
	// filter
	if (recv_msg_buf.idjob != idjob){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER idjob \n");}
		return -1;
	}
	else if ((src != MPI_ANY_SOURCE) && (recv_msg_buf.src != src)){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER src \n");}
		return -1;
	}
	else if ((tag != MPI_ANY_TAG) && (recv_msg_buf.tag != tag)) {
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER tag \n");}
		return -1;
	} 
	else if (recv_msg_buf.datatype != datatype){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER datatype \n");}
		return -1;
	} 
	else{
		switch (datatype){
		case  MPI_CHAR:  case MPI_UNSIGNED_CHAR: case MPI_BYTE:
			if (DEBUG) {
				fprintf(mslog, "MPI_CHAR Datatype found  \n");}
			length = sizeof(char) * count; 
			break;
		case  MPI_INT:  case MPI_UNSIGNED:
			if (DEBUG) {
				fprintf(mslog, "MPI_INT Datatype found  \n");}
			length = sizeof(int) * count;
			break;
		case MPI_SHORT: case MPI_UNSIGNED_SHORT:
			if (DEBUG) {
				fprintf(mslog, "MPI_SHORT Datatype found  \n");}
			length = sizeof(short) * count;
			break; 
		case MPI_LONG: case MPI_UNSIGNED_LONG:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG Datatype found  \n");}
			length = sizeof(long) * count;
			break; 
		case MPI_FLOAT:
			if (DEBUG) {
				fprintf(mslog, "MPI_FLOAT Datatype found  \n");}
			length = sizeof(float) * count;
			break; 
		case MPI_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_DOUBLE Datatype found  \n");}
			length = sizeof(double) * count;
			break; 
			
		case MPI_LONG_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG_DOUBLE Datatype found  \n");}
			length = sizeof(long double) * count;
			break; 
		case MPI_LONG_LONG:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG_LONG Datatype found  \n");}
			length = sizeof(long long) * count;
			break; 
						
		default:
			if (DEBUG) {fprintf(mslog, "!!! BAD DATATYPE \n");}
		return -1;
		}
		memcpy(r->buf, recv_msg_buf.data, length);
	}
	return 0;
}

/*
 * ProActiveMPI_Test
 */
int ProActiveMPI_Test(ProActiveMPI_Request *r, int* flag){
	msg_t recv_msg_buf;
	int error = -1;
	int pms;
	int length;
	
	int idjob = (*r).idjob;
	int tag = (*r).tag;
	int count = (*r).count;
	int datatype = (*r).datatype;
	int src=(*r).src;
	
	pms= sizeof(msg_t) - sizeof(recv_msg_buf.TAG) - sizeof(recv_msg_buf.data);
	
	error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, IPC_NOWAIT);
	while (error < 0){
		strerror(errno);
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
		
		if (errno == EINTR){
			if (DEBUG) { fprintf(mslog, "!!! ERRNO = EINTR, \n");}
			error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, IPC_NOWAIT);
		}
		// no message in the queue
		else if (errno == ENOMSG){
			if (DEBUG) {
				fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
			// mv buffer pointer
			*flag = 0; // not recv yet
			return 0;
		}
		else{
			perror("ERROR");
			return -1; 
		}
	}
	// Msg recved
	*flag = 1;
	
	if (DEBUG) {fflush(mslog);}
	// filter
	if (recv_msg_buf.idjob != idjob){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER idjob \n");}
		return -1;
	}
	else if ((src != MPI_ANY_SOURCE) && (recv_msg_buf.src != src)){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER src \n");}
		return -1;
	}
	else if ((tag != MPI_ANY_TAG) && (recv_msg_buf.tag != tag)) {
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER tag \n");}
		return -1;
	} 
	else if (recv_msg_buf.datatype != datatype){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER datatype \n");}
		return -1;
	} 
	else{
		switch (datatype){
		case  MPI_CHAR:  case MPI_UNSIGNED_CHAR: case MPI_BYTE:
			if (DEBUG) {
				fprintf(mslog, "MPI_CHAR Datatype found  \n");}
			length = sizeof(char) * count; 
			break;
		case  MPI_INT:  case MPI_UNSIGNED:
			if (DEBUG) {
				fprintf(mslog, "MPI_INT Datatype found  \n");}
			length = sizeof(int) * count;
			break;
		case MPI_SHORT: case MPI_UNSIGNED_SHORT:
			if (DEBUG) {
				fprintf(mslog, "MPI_SHORT Datatype found  \n");}
			length = sizeof(short) * count;
			break; 
		case MPI_LONG: case MPI_UNSIGNED_LONG:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG Datatype found  \n");}
			length = sizeof(long) * count;
			break; 
		case MPI_FLOAT:
			if (DEBUG) {
				fprintf(mslog, "MPI_FLOAT Datatype found  \n");}
			length = sizeof(float) * count;
			break; 
		case MPI_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_DOUBLE Datatype found  \n");}
			length = sizeof(double) * count;
			break; 
			
		case MPI_LONG_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG_DOUBLE Datatype found  \n");}
			length = sizeof(long double) * count;
			break; 
		case MPI_LONG_LONG:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG_LONG Datatype found  \n");}
			length = sizeof(long long) * count;
			break; 
						
		default:
			if (DEBUG) {fprintf(mslog, "!!! BAD DATATYPE \n");}
		return -1;
		}
		memcpy(r->buf, recv_msg_buf.data, length);	
	}
	return 0;
}

/*
 * ProActiveMPI_AllSend
 */
int ProActiveMPI_AllSend( void * buf, int count, MPI_Datatype datatype, int tag, int idjob)
{ 
	msg_t send_msg_buf;
	int error;
	int pms;
	int length;
	
	if (DEBUG) {
		fprintf(mslog, "!!! ProActiveMPI_AllSend \n");}
	send_msg_buf.TAG1 = MSG_ALLSEND;
	send_msg_buf.count = count;
	send_msg_buf.src = myRank ;
	send_msg_buf.dest = -1;
	send_msg_buf.datatype = datatype;
	send_msg_buf.tag = tag;
	send_msg_buf.TAG = TAG_S_KEY;
	send_msg_buf.idjob = idjob;
	
	switch (datatype){
	case  MPI_CHAR:  case MPI_UNSIGNED_CHAR: case MPI_BYTE:
		if (DEBUG) {
			fprintf(mslog, "MPI_CHAR Datatype found  \n");}
		length = sizeof(char) * count; 
		break;
	case  MPI_INT:  case MPI_UNSIGNED:
		if (DEBUG) {
			fprintf(mslog, "MPI_INT Datatype found  \n");}
		length = sizeof(int) * count;
		break;
	case MPI_SHORT: case MPI_UNSIGNED_SHORT:
		if (DEBUG) {
			fprintf(mslog, "MPI_SHORT Datatype found  \n");}
		length = sizeof(short) * count;
		break; 
	case MPI_LONG: case MPI_UNSIGNED_LONG:
		if (DEBUG) {
			fprintf(mslog, "MPI_LONG Datatype found - Length = %d \n", sizeof(long) * count);}
		length = sizeof(long) * count;
		break; 
	case MPI_FLOAT:
		if (DEBUG) {
			fprintf(mslog, "MPI_FLOAT Datatype found  \n");}
		length = sizeof(float) * count;
		break; 
	case MPI_DOUBLE:
		if (DEBUG) {
			fprintf(mslog, "MPI_DOUBLE Datatype found  \n");}
		length = sizeof(double) * count;
		break; 
		
	case MPI_LONG_DOUBLE:
		if (DEBUG) {
			fprintf(mslog, "MPI_LONG_DOUBLE Datatype found  \n");}
		length = sizeof(long double) * count;
		break; 
	case MPI_LONG_LONG:
		if (DEBUG) {
			fprintf(mslog, "MPI_LONG_LONG Datatype found  \n");}
		length = sizeof(long long) * count;
		break; 
	default:
		if (DEBUG) {fprintf(mslog, "!!! BAD DATATYPE \n");}
	return -1;
	}
	memcpy(send_msg_buf.data, buf, length);
	pms= sizeof(msg_t) - sizeof(send_msg_buf.TAG) - sizeof(send_msg_buf.data);
	error = msgsnd(C2S_Q_ID, &send_msg_buf, pms+length, 0);
	if (error < 0) {
		if (DEBUG) {fprintf(mslog, "!!! ERROR: msgsnd error\n");}
		perror("ERROR"); 
		return -1;  }
	if (DEBUG) {fflush(mslog);}
	return 0;
}




/*
 * ProActiveMPI_Barrier
 */
int ProActiveMPI_Barrier(int job){
	if (job == myJob) { 
		return MPI_Barrier(MPI_COMM_WORLD);}
	else
		return -1;
}

/*
 * ProActiveMPI_Finalize
 */
int ProActiveMPI_Finalize(){
	msg_t send_msg_buf;
	int error;
	int pms;
	send_msg_buf.TAG1 = MSG_FINALIZE;
	send_msg_buf.TAG = TAG_S_KEY;
	strcpy(send_msg_buf.data, "");
	pms= sizeof(msg_t) - sizeof(send_msg_buf.TAG) - sizeof(send_msg_buf.data);
	error = msgsnd(C2S_Q_ID, &send_msg_buf, pms+1, 0);
	if (error < 0) {
		if (DEBUG) {fprintf(mslog, "!!! ERROR: msgsnd error\n");}
		perror("ERROR");
		return -1; }
	if (DEBUG) {fflush(mslog);}
	return 0;
}


/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+----- MPI -> PROACTIVE FUNCTIONS  -+-----+- */
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

/*
 * ProActiveSend
 */
int ProActiveSend(void* buf, int count, MPI_Datatype datatype, int dest, char* clazz, char* method, int idjob, ...){
	msg_t send_msg_buf;
	int error;
	int pms;
	int length;
	char* next;
	int nb_args = 0;
	char * nb = (char *) malloc(sizeof(char)*2);
	char * parameters = (char *) malloc(50);
	va_list ptr;
	
	strcpy(parameters, "");
	strcpy(send_msg_buf.method, "");
	// ptr initialization
	va_start(ptr, idjob);  
	next = va_arg(ptr, char*);
	while (next != NULL) {
		nb_args++;
		strcat(parameters, next);
		strcat(parameters, "\t");
		next = va_arg(ptr, char*);	
	}
	sprintf(nb, "%d", nb_args);
	send_msg_buf.TAG1 = MSG_SEND_PROACTIVE;
	send_msg_buf.count = count;
	send_msg_buf.src = myRank ;
	send_msg_buf.dest = dest;
	send_msg_buf.datatype = datatype;
	send_msg_buf.TAG = TAG_S_KEY;
	send_msg_buf.idjob = idjob;
	strcpy(send_msg_buf.method,clazz);
	strcat(send_msg_buf.method,"\t");
	strcat(send_msg_buf.method,method);
	strcat(send_msg_buf.method,"\t");
	strcat(send_msg_buf.method,nb);
	strcat(send_msg_buf.method,"\t");
	strcat(send_msg_buf.method,parameters);
	
	switch (datatype){
	case  MPI_CHAR:  case MPI_UNSIGNED_CHAR: case MPI_BYTE:
		if (DEBUG) {
			fprintf(mslog, "MPI_CHAR Datatype found  \n");}
		length = sizeof(char) * count; 
		break;
	case  MPI_INT:  case MPI_UNSIGNED:
		if (DEBUG) {
			fprintf(mslog, "MPI_INT Datatype found  \n");}
		length = sizeof(int) * count;
		break;
	case MPI_SHORT: case MPI_UNSIGNED_SHORT:
		if (DEBUG) {
			fprintf(mslog, "MPI_SHORT Datatype found  \n");}
		length = sizeof(short) * count;
		break; 
	case MPI_LONG: case MPI_UNSIGNED_LONG:
		if (DEBUG) {
			fprintf(mslog, "MPI_LONG Datatype found  \n");}
		length = sizeof(long) * count;
		break; 
	case MPI_FLOAT:
		if (DEBUG) {
			fprintf(mslog, "MPI_FLOAT Datatype found  \n");}
		length = sizeof(float) * count;
		break; 
	case MPI_DOUBLE:
		if (DEBUG) {
			fprintf(mslog, "MPI_DOUBLE Datatype found  \n");}
		length = sizeof(double) * count;
		break; 	
	case MPI_LONG_DOUBLE:
		if (DEBUG) {
			fprintf(mslog, "MPI_LONG_DOUBLE Datatype found  \n");}
		length = sizeof(long double) * count;
		break; 
	case MPI_LONG_LONG:
		if (DEBUG) {
			fprintf(mslog, "MPI_LONG_LONG Datatype found  \n");}
		length = sizeof(long long) * count;
		break; 
		
	default:
		if (DEBUG) {fprintf(mslog, "!!! BAD DATATYPE \n");}
	return -1;
	}
	memcpy(send_msg_buf.data, buf, length);
	pms= sizeof(msg_t) - sizeof(send_msg_buf.TAG) - sizeof(send_msg_buf.data);
	error = msgsnd(C2S_Q_ID, &send_msg_buf, pms+length, 0);
	if (error < 0) {
		if (DEBUG) {fprintf(mslog, "!!! ERROR: msgsnd error\n");}
		perror("ERROR"); 
		return -1;  }
	if (DEBUG) {fflush(mslog);}
	
	return 0;
}

/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+----- PROACTIVE -> MPI  FUNCTIONS  -+-----+- */
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

/*
 * ProActiveRecv
 */
int ProActiveRecv(void* buf, int count, MPI_Datatype datatype, int src, int tag, int idjob){
	msg_t recv_msg_buf;
	int error = -1;
	int pms;
	int length;
	
	pms= sizeof(msg_t) - sizeof(recv_msg_buf.TAG) - sizeof(recv_msg_buf.data);
	
	error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, PROACTIVE_KEY, 0);
	// if an error occured during receive call check if its an interrupted
	// System call and so retry to receive
	while (error < 0){
		strerror(errno);
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
		
		if (errno == EINTR){
			if (DEBUG) { fprintf(mslog, "!!! ERRNO = EINTR, \n");}
			error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, PROACTIVE_KEY, 0);
			if (DEBUG) {
				fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
		}
		else{
			perror("ERROR");
			return -1; 
		}
	}
	if (DEBUG) {fflush(mslog);}
	if (recv_msg_buf.idjob != idjob){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER idjob \n");}
		return -1;
	}
// else if (recv_msg_buf.src != src){
// if (DEBUG) {
// fprintf(mslog, "!!! ERROR: BAD PARAMETER src \n");}
// return -1;
// }
	else if ((tag != MPI_ANY_TAG) && (recv_msg_buf.tag != tag)) {
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER tag \n");}
		return -1;
	} 
	else if (recv_msg_buf.datatype != datatype){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER datatype \n");}
		return -1;
	} 
	else{
		switch (datatype){
		case  MPI_CHAR:  case MPI_UNSIGNED_CHAR: case MPI_BYTE:
			if (DEBUG) {
				fprintf(mslog, "MPI_CHAR Datatype found  \n");}
			length = sizeof(char) * count; 
			break;
		case  MPI_INT:  case MPI_UNSIGNED:
			if (DEBUG) {
				fprintf(mslog, "MPI_INT Datatype found  \n");}
			length = sizeof(int) * count;
			break;
		case MPI_SHORT: case MPI_UNSIGNED_SHORT:
			if (DEBUG) {
				fprintf(mslog, "MPI_SHORT Datatype found  \n");}
			length = sizeof(short) * count;
			break; 
		case MPI_LONG: case MPI_UNSIGNED_LONG:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG Datatype found  \n");}
			length = sizeof(long) * count;
			break; 
		case MPI_FLOAT:
			if (DEBUG) {
				fprintf(mslog, "MPI_FLOAT Datatype found  \n");}
			length = sizeof(float) * count;
			break; 
		case MPI_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_DOUBLE Datatype found  \n");}
			length = sizeof(double) * count;
			break; 
			
		case MPI_LONG_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG_DOUBLE Datatype found  \n");}
			length = sizeof(long double) * count;
			break; 
		case MPI_LONG_LONG:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG_LONG Datatype found  \n");}
			length = sizeof(long long) * count;
			break; 
			// case MPI_LOGICAL: break;
			// case MPI_PACKED: break;
			
		default:
			if (DEBUG) {fprintf(mslog, "!!! BAD DATATYPE \n");}
		return -1;
		}
		memcpy(buf, recv_msg_buf.data, length);
	}
	return 0;
}

/*
 * ProActiveWait
 */
int ProActiveWait(ProActiveMPI_Request *r){
	msg_t recv_msg_buf;
	int error = -1;
	int pms;
	int length;
	
	int idjob = (*r).idjob;
	int tag = (*r).tag;
	int count = (*r).count;
	int datatype = (*r).datatype;
	
	pms= sizeof(msg_t) - sizeof(recv_msg_buf.TAG) - sizeof(recv_msg_buf.data);
	
	error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, PROACTIVE_KEY, 0);
	while (error < 0){
		strerror(errno);
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
		
		if (errno == EINTR){
			if (DEBUG) { fprintf(mslog, "!!! ERRNO = EINTR, \n");}
			error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, PROACTIVE_KEY, 0);
		}
		// no message in the queue
		else{
			perror("ERROR");
			return -1; 
		}
	}
	
	if (DEBUG) {fflush(mslog);}
	// filter
	if (recv_msg_buf.idjob != idjob){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER idjob \n");}
		return -1;
	}
// else if (recv_msg_buf.src != src){
// if (DEBUG) {
// fprintf(mslog, "!!! ERROR: BAD PARAMETER src \n");}
// return -1;
// }
	else if ((tag != MPI_ANY_TAG) && (recv_msg_buf.tag != tag)) {
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER tag \n");}
		return -1;
	} 
	else if (recv_msg_buf.datatype != datatype){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER datatype \n");}
		return -1;
	} 
	else{
		switch (datatype){
		case  MPI_CHAR:  case MPI_UNSIGNED_CHAR: case MPI_BYTE:
			if (DEBUG) {
				fprintf(mslog, "MPI_CHAR Datatype found  \n");}
			length = sizeof(char) * count; 
			break;
		case  MPI_INT:  case MPI_UNSIGNED:
			if (DEBUG) {
				fprintf(mslog, "MPI_INT Datatype found  \n");}
			length = sizeof(int) * count;
			break;
		case MPI_SHORT: case MPI_UNSIGNED_SHORT:
			if (DEBUG) {
				fprintf(mslog, "MPI_SHORT Datatype found  \n");}
			length = sizeof(short) * count;
			break; 
		case MPI_LONG: case MPI_UNSIGNED_LONG:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG Datatype found  \n");}
			length = sizeof(long) * count;
			break; 
		case MPI_FLOAT:
			if (DEBUG) {
				fprintf(mslog, "MPI_FLOAT Datatype found  \n");}
			length = sizeof(float) * count;
			break; 
		case MPI_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_DOUBLE Datatype found  \n");}
			length = sizeof(double) * count;
			break; 
			
		case MPI_LONG_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG_DOUBLE Datatype found  \n");}
			length = sizeof(long double) * count;
			break; 
		case MPI_LONG_LONG:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG_LONG Datatype found  \n");}
			length = sizeof(long long) * count;
			break; 
						
		default:
			if (DEBUG) {fprintf(mslog, "!!! BAD DATATYPE \n");}
		return -1;
		}
		memcpy(r->buf, recv_msg_buf.data, length);	
	}
	return 0;	
}

/*
 * ProActiveTest
 */
int ProActiveTest(ProActiveMPI_Request *r, int* flag){
	msg_t recv_msg_buf;
	int error = -1;
	int pms;
	int length;
	
	int idjob = (*r).idjob;
	int tag = (*r).tag;
	int count = (*r).count;
	int datatype = (*r).datatype;
	
	pms= sizeof(msg_t) - sizeof(recv_msg_buf.TAG) - sizeof(recv_msg_buf.data);
	
	error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, PROACTIVE_KEY, IPC_NOWAIT);
	while (error < 0){
		strerror(errno);
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
		
		if (errno == EINTR){
			if (DEBUG) { fprintf(mslog, "!!! ERRNO = EINTR, \n");}
			error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, PROACTIVE_KEY, IPC_NOWAIT);
		}
		// no message in the queue
		else if (errno == ENOMSG){
			if (DEBUG) {
				fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
			// mv buffer pointer
			*flag = 0; // not recv yet
			return 0;
		}
		else{
			perror("ERROR");
			return -1; 
		}
	}
	// Msg recved
	*flag = 1;
	
	if (DEBUG) {fflush(mslog);}
	// filter
	if (recv_msg_buf.idjob != idjob){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER idjob \n");}
		return -1;
	}
// else if (recv_msg_buf.src != src){
// if (DEBUG) {
// fprintf(mslog, "!!! ERROR: BAD PARAMETER src \n");}
// return -1;
// }
	else if ((tag != MPI_ANY_TAG) && (recv_msg_buf.tag != tag)) {
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER tag \n");}
		return -1;
	} 
	else if (recv_msg_buf.datatype != datatype){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER datatype \n");}
		return -1;
	} 
	else{
		switch (datatype){
		case  MPI_CHAR:  case MPI_UNSIGNED_CHAR: case MPI_BYTE:
			if (DEBUG) {
				fprintf(mslog, "MPI_CHAR Datatype found  \n");}
			length = sizeof(char) * count; 
			break;
		case  MPI_INT:  case MPI_UNSIGNED:
			if (DEBUG) {
				fprintf(mslog, "MPI_INT Datatype found  \n");}
			length = sizeof(int) * count;
			break;
		case MPI_SHORT: case MPI_UNSIGNED_SHORT:
			if (DEBUG) {
				fprintf(mslog, "MPI_SHORT Datatype found  \n");}
			length = sizeof(short) * count;
			break; 
		case MPI_LONG: case MPI_UNSIGNED_LONG:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG Datatype found  \n");}
			length = sizeof(long) * count;
			break; 
		case MPI_FLOAT:
			if (DEBUG) {
				fprintf(mslog, "MPI_FLOAT Datatype found  \n");}
			length = sizeof(float) * count;
			break; 
		case MPI_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_DOUBLE Datatype found  \n");}
			length = sizeof(double) * count;
			break; 
			
		case MPI_LONG_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG_DOUBLE Datatype found  \n");}
			length = sizeof(long double) * count;
			break; 
		case MPI_LONG_LONG:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG_LONG Datatype found  \n");}
			length = sizeof(long long) * count;
			break; 
						
		default:
			if (DEBUG) {fprintf(mslog, "!!! BAD DATATYPE \n");}
		return -1;
		}
		memcpy(r->buf, recv_msg_buf.data, length);	
	}
	return 0;
}
	
/*
 * ProActiveIRecv
 */
int ProActiveIRecv(void* buf, int count, MPI_Datatype datatype, int src, int tag, int idjob, ProActiveMPI_Request *r){
	msg_t recv_msg_buf;
	int error = -1;
	int pms;
	int length;
	
	pms= sizeof(msg_t) - sizeof(recv_msg_buf.TAG) - sizeof(recv_msg_buf.data);
	
	error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, PROACTIVE_KEY, IPC_NOWAIT);
	
	while (error < 0){
		strerror(errno);
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
		
		if (errno == EINTR){
			if (DEBUG) { fprintf(mslog, "!!! ERRNO = EINTR, \n");}
			error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, PROACTIVE_KEY, IPC_NOWAIT);
		}
		// no message in the queue
		else if (errno == ENOMSG){
			if (DEBUG) {
				fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
			r->buf = buf; // keep buf address in structure to update it later
			(*r).flag = 0; // nothing recv yet
			// keep parameters for further recv
			(*r).count = count;
			(*r).datatype = datatype;
			(*r).src = src;
			(*r).tag = tag;
			(*r).idjob = idjob;
			return 0;
		}
		else{
			perror("[ProActiveIRecv]!!! ERROR");
			return -1; 
		}
	}
	
	if (DEBUG) {fflush(mslog);}
	// filter
	if (recv_msg_buf.idjob != idjob){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER idjob \n");}
		return -1;
	}
// else if (recv_msg_buf.src != src){
// if (DEBUG) {
// fprintf(mslog, "!!! ERROR: BAD PARAMETER src \n");}
// return -1;
// }
	else if (recv_msg_buf.tag != tag) {
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER tag \n");}
		return -1;
	} 
	else if (recv_msg_buf.datatype != datatype){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER datatype \n");}
		return -1;
	} 
	else{
		switch (datatype){
		case  MPI_CHAR:  case MPI_UNSIGNED_CHAR: case MPI_BYTE:
			if (DEBUG) {
				fprintf(mslog, "MPI_CHAR Datatype found  \n");}
			length = sizeof(char) * count; 
			break;
		case  MPI_INT:  case MPI_UNSIGNED:
			if (DEBUG) {
				fprintf(mslog, "MPI_INT Datatype found  \n");}
			length = sizeof(int) * count;
			break;
		case MPI_SHORT: case MPI_UNSIGNED_SHORT:
			if (DEBUG) {
				fprintf(mslog, "MPI_SHORT Datatype found  \n");}
			length = sizeof(short) * count;
			break; 
		case MPI_LONG: case MPI_UNSIGNED_LONG:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG Datatype found  \n");}
			length = sizeof(long) * count;
			break; 
		case MPI_FLOAT:
			if (DEBUG) {
				fprintf(mslog, "MPI_FLOAT Datatype found  \n");}
			length = sizeof(float) * count;
			break; 
		case MPI_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_DOUBLE Datatype found  \n");}
			length = sizeof(double) * count;
			break; 
			
		case MPI_LONG_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG_DOUBLE Datatype found  \n");}
			length = sizeof(long double) * count;
			break; 
		case MPI_LONG_LONG:
			if (DEBUG) {
				fprintf(mslog, "MPI_LONG_LONG Datatype found  \n");}
			length = sizeof(long long) * count;
			break; 
						
		default:
			if (DEBUG) {fprintf(mslog, "!!! BAD DATATYPE \n");}
		return -1;
		}
		memcpy(buf, recv_msg_buf.data, length);
		
		
	}
	
	return 0;
	
}




// /////////////////////////////////////////////////////////
// ///////////////// F77 IMPLEMENTATION /////////////////////
// //////////////////////////////////////////////////////////


void proactivempi_init_(int * rank, int* ierr){
	msg_t send_msg_buf, recv_msg_buf ;
	int error;
	int pms;
	char path[256];
	strcpy(path,"");
	strcpy(path,"/tmp");
	myRank = *rank;
	if (DEBUG){  
		if (openlog(path, *rank) < 0){ printf("ERROR WHILE OPENING FILE PATH= %s \n", path); perror("ERROR");  exit(1);}
		fprintf(mslog, "Initializing queues \n");
	}
	
	if ((C2S_Q_ID = msgget(C2S_KEY,  ACCESS_PERM)) == -1) {
		perror("ERROR ");
		if (DEBUG){
			fprintf(mslog, "Cannot open sending queue: %d   \n",C2S_Q_ID);
		}
		*ierr=-1;
		return;
	}
	else {
		if (DEBUG){ 
			fprintf(mslog, "Sending Queue %d successfully opened \n ",C2S_Q_ID); }
	}
	if ((S2C_Q_ID = msgget(S2C_KEY,  ACCESS_PERM)) == -1) {
		perror("ERROR ");
		if (DEBUG){
			fprintf(mslog, "Cannot open receiving queue: %d   \n",S2C_Q_ID);
		}
		*ierr=-1;
		return;
	}
	else {
		if (DEBUG){
			fprintf(mslog, "Receivind Queue %d successfully opened \n ",S2C_Q_ID); }
	}
	send_msg_buf.TAG1 = MSG_INIT;
	send_msg_buf.TAG = TAG_S_KEY;
	send_msg_buf.src = *rank;
// strcpy(send_msg_buf.data, "");
	pms= sizeof(msg_t) - sizeof(send_msg_buf.TAG) - sizeof(send_msg_buf.data);
	error = msgsnd(C2S_Q_ID, &send_msg_buf, pms+1, 0);
	if (error < 0) {
		if (DEBUG) {fprintf(mslog, "!!! ERROR: msgsnd error\n");}
		perror("ERROR");
		*ierr=-1;
		return; }
	
	if (DEBUG){
		fprintf(mslog, "Waiting for job number in recv queue \n "); }
	error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, 0);
	// if an error occured during receive call check if its an interrupted
	// System call and so retry to receive
	while (error < 0){
		strerror(errno);
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
		
		if (errno == EINTR){
			if (DEBUG) { fprintf(mslog, "!!! ERRNO = EINTR, \n");}
			error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, 0);
		}
		else{
			perror("ERROR");
			*ierr=-1;
			return; 
		}
	}
	
	myJob = recv_msg_buf.idjob;
	
	if (DEBUG) {fflush(mslog);}
	*ierr=0;
}

void proactivempi_send_(void * buf, int* cnt, MPI_Datatype* datatype, int* dest, int* tag, int* idjob, int* ierr)
{ 
	msg_t send_msg_buf;
	int error;
	int pms;
	int length;
	int count = *cnt;
	send_msg_buf.TAG1 = MSG_SEND;
	send_msg_buf.count = *cnt;
	send_msg_buf.src = myRank ;
	send_msg_buf.dest = *dest;
	send_msg_buf.datatype = *datatype;
	send_msg_buf.tag = *tag;
	send_msg_buf.TAG = TAG_S_KEY;
	send_msg_buf.idjob = *idjob;
	
	switch (*datatype){
	case  MPI_CHARACTER:
		if (DEBUG) {
			fprintf(mslog, "MPI_CHAR Datatype found \n");}
		length = sizeof(char) * count; 
		break;
	case  MPI_INTEGER:
		if (DEBUG) {
			fprintf(mslog, "MPI_INT Datatype found \n");}
		length = sizeof(int) * count;
		break;
	case MPI_DOUBLE:
		if (DEBUG) {
			fprintf(mslog, "MPI_DOUBLE Datatype found  \n");}
		length = sizeof(double) * count;
		break; 
		
	default:
		if (DEBUG) {fprintf(mslog, "!!! BAD DATATYPE \n");}
	 	*ierr=-1;
	}
	memcpy(send_msg_buf.data, buf, length);
	pms= sizeof(msg_t) - sizeof(send_msg_buf.TAG) - sizeof(send_msg_buf.data);
	error = msgsnd(C2S_Q_ID, &send_msg_buf, pms+length, 0);
	if (error < 0) {
		if (DEBUG) {fprintf(mslog, "!!! ERROR: msgsnd error\n");}
		perror("ERROR"); 
		*ierr= -1;
		return;}
	if (DEBUG) {fflush(mslog);}
	*ierr=0;
}



void proactivempi_recv_(void* buf, int* cnt, MPI_Datatype* datatype, int* src, int* tag, int* idjob, int* ierr){
	msg_t recv_msg_buf;
	int error = -1;
	int pms;
	int length;
	int count = *cnt;
	
	pms= sizeof(msg_t) - sizeof(recv_msg_buf.TAG) - sizeof(recv_msg_buf.data);
	
	error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, 0);
	// if an error occured during receive call check if its an interrupted
	// System call and so retry to receive
	while (error < 0){
		strerror(errno);
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
		
		if (errno == EINTR){
			if (DEBUG) { fprintf(mslog, "!!! ERRNO = EINTR, \n");}
			error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, 0);
		}
		else{
			perror("ERROR");
			*ierr=-1; 
			return; 
		}
	}
	
	if (DEBUG) {fflush(mslog);}
	// filter
	if (recv_msg_buf.idjob != *idjob){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER idjob \n");}
		*ierr=-1; 
		return; 
	}
	else if (recv_msg_buf.src != *src){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER src \n");}
		*ierr=-1; 
		return; 
	}
	else if (recv_msg_buf.tag != *tag) {
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER tag \n");}
		*ierr=-1; 
		return; 
	} 
	else if (recv_msg_buf.datatype != *datatype){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER datatype \n");}
		*ierr=-1; 
		return; 
	} 
	else{
		switch (*datatype){
		case  MPI_CHARACTER:
			if (DEBUG) {
				fprintf(mslog, "MPI_CHAR Datatype found  \n");}
			length = sizeof(char) * count; 
			break;
		case  MPI_INTEGER:
			if (DEBUG) {
				fprintf(mslog, "MPI_INT Datatype found \n");}
			length = sizeof(int) * count;
			break;
		case MPI_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_DOUBLE Datatype found  \n");}
			length = sizeof(double) * count;
			break; 
		default:
			if (DEBUG) {fprintf(mslog, "!!! BAD DATATYPE \n");}
		*ierr=-1; 
		return; 
		}
		memcpy(buf, recv_msg_buf.data, length);
	}
	
	*ierr=0; 
}



void proactivempi_irecv_(void* buf, int* cnt, MPI_Datatype* datatype, int* src, int* tag, int* idjob, ProActiveMPI_Request* r, int* ierr){
	msg_t recv_msg_buf;
	int error = -1;
	int pms;
	int length;
	int count = *cnt;
	pms= sizeof(msg_t) - sizeof(recv_msg_buf.TAG) - sizeof(recv_msg_buf.data);
	error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, IPC_NOWAIT);
	while (error < 0){
		strerror(errno);
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
		if (errno == EINTR){
			if (DEBUG) { fprintf(mslog, "!!! ERRNO = EINTR, \n");}
			error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, IPC_NOWAIT);
		}
		// no message in the queue
		else if (errno == ENOMSG){
			if (DEBUG) {
				fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
			r->buf = buf; // keep buf address in structure to update it later
			(*r).flag = 0; // nothing recv yet
			// keep parameters for further recv
			(*r).count = *cnt;
			(*r).datatype = *datatype;
			(*r).src = *src;
			(*r).tag = *tag;
			(*r).idjob = *idjob;
			*ierr=0;
		}
		else{
			perror("[ProActiveMPI_IRecv]!!! ERROR");
			*ierr=-1;
			return;  
		}
	}
	
	if (DEBUG) {fflush(mslog);}
	// filter
	if (recv_msg_buf.idjob != *idjob){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER idjob \n");}
		*ierr=-1;
		return;
	}
	else if ((*src != MPI_ANY_SOURCE) && (recv_msg_buf.src != *src)){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER src \n");}
		*ierr=-1;
		return;
	}
	else if ((*tag != MPI_ANY_TAG) && (recv_msg_buf.tag != *tag)) {
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER tag \n");}
		*ierr = -1;
		return;
	} 
	else if (recv_msg_buf.datatype != *datatype){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER datatype \n");}
		*ierr=-1;
	} 
	else{
		switch (*datatype){
		case  MPI_CHARACTER: case MPI_BYTE:
			if (DEBUG) {
				fprintf(mslog, "MPI_CHAR Datatype found  \n");}
			length = sizeof(char) * count; 
			break;
		case  MPI_INTEGER:
			if (DEBUG) {
				fprintf(mslog, "MPI_INT Datatype found  \n");}
			length = sizeof(int) * count;
			break;
		case MPI_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_DOUBLE Datatype found  \n");}
			length = sizeof(double) * count;
			break; 						
		default:
			if (DEBUG) {fprintf(mslog, "!!! BAD DATATYPE \n");}
		*ierr = -1;
		return;
		}
		memcpy(buf, recv_msg_buf.data, length);	
	}
	*ierr = 0;
	return;
}

void proactivempi_test_(ProActiveMPI_Request *r, int* flag, int* ierr){
	msg_t recv_msg_buf;
	int error = -1;
	int pms;
	int length;
	
	int idjob = (*r).idjob;
	int tag = (*r).tag;
	int count = (*r).count;
	int datatype = (*r).datatype;
	int src=(*r).src;
	
	pms= sizeof(msg_t) - sizeof(recv_msg_buf.TAG) - sizeof(recv_msg_buf.data);
	
	error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, IPC_NOWAIT);
	while (error < 0){
		strerror(errno);
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
		
		if (errno == EINTR){
			if (DEBUG) { fprintf(mslog, "!!! ERRNO = EINTR, \n");}
			error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, IPC_NOWAIT);
		}
		// no message in the queue
		else if (errno == ENOMSG){
			if (DEBUG) {
				fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
			// mv buffer pointer
			*flag = 0; // not recv yet
			*ierr = 0;
			return;
		}
		else{
			perror("ERROR");
			*ierr = -1;
			return;
		}
	}
	// Msg recved
	*flag = 1;
	
	if (DEBUG) {fflush(mslog);}
	// filter
	if (recv_msg_buf.idjob != idjob){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER idjob \n");}
		*ierr = -1;
		return;
	}
	else if ((src != MPI_ANY_SOURCE) && (recv_msg_buf.src != src)){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER src \n");}
		*ierr = -1;
		return;
	}
	else if ((tag != MPI_ANY_TAG) && (recv_msg_buf.tag != tag)) {
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER tag \n");}
		*ierr= -1;
		return;
	} 
	else if (recv_msg_buf.datatype != datatype){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER datatype \n");}
		*ierr=-1;
		return;
	} 
	else{
		switch (datatype){
		case  MPI_CHARACTER: case MPI_BYTE:
			if (DEBUG) {
				fprintf(mslog, "MPI_CHAR Datatype found  \n");}
			length = sizeof(char) * count; 
			break;
		case  MPI_INTEGER:
			if (DEBUG) {
				fprintf(mslog, "MPI_INT Datatype found  \n");}
			length = sizeof(int) * count;
			break;
		case MPI_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_DOUBLE Datatype found  \n");}
			length = sizeof(double) * count;
			break; 
		default:
			if (DEBUG) {fprintf(mslog, "!!! BAD DATATYPE \n");}
		*ierr=-1;
		return;
		}
		memcpy(r->buf, recv_msg_buf.data, length);	
	}
	*ierr=0;
	return;
}

void proactivempi_wait_(ProActiveMPI_Request *r, int* ierr){
	msg_t recv_msg_buf;
	int error = -1;
	int pms;
	int length;
	
	int idjob = (*r).idjob;
	int tag = (*r).tag;
	int count = (*r).count;
	int datatype = (*r).datatype;
	int src = (*r).src;
	
	pms= sizeof(msg_t) - sizeof(recv_msg_buf.TAG) - sizeof(recv_msg_buf.data);
	
	error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, 0);
	while (error < 0){
		strerror(errno);
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
		
		if (errno == EINTR){
			if (DEBUG) { fprintf(mslog, "!!! ERRNO = EINTR, \n");}
			error = msgrcv(S2C_Q_ID, &recv_msg_buf, pms+MSG_SIZE, TAG_R_KEY, 0);
		}
		// no message in the queue
		else{
			perror("ERROR");
			*ierr = -1;
			return;
		}
	}
	
	if (DEBUG) {fflush(mslog);}
	// filter
	if (recv_msg_buf.idjob != idjob){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER idjob \n");}
		*ierr = -1;
		return;
	}
	else if ((src != MPI_ANY_SOURCE) && (recv_msg_buf.src != src)){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER src \n");}
		*ierr = -1;
		return;
	}
	else if ((tag != MPI_ANY_TAG) && (recv_msg_buf.tag != tag)) {
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER tag \n");}
		*ierr = -1;
		return;
	} 
	else if (recv_msg_buf.datatype != datatype){
		if (DEBUG) {
			fprintf(mslog, "!!! ERROR: BAD PARAMETER datatype \n");}
		*ierr = -1;
		return;
	} 
	else{
		switch (datatype){
		case  MPI_CHARACTER: case MPI_BYTE:
			if (DEBUG) {
				fprintf(mslog, "MPI_CHAR Datatype found  \n");}
			length = sizeof(char) * count; 
			break;
		case  MPI_INTEGER:
			if (DEBUG) {
				fprintf(mslog, "MPI_INT Datatype found  \n");}
			length = sizeof(int) * count;
			break;
		case MPI_DOUBLE:
			if (DEBUG) {
				fprintf(mslog, "MPI_DOUBLE Datatype found  \n");}
			length = sizeof(double) * count;
			break; 

		default:
			if (DEBUG) {fprintf(mslog, "!!! BAD DATATYPE \n");}
		*ierr = -1;
		return;
		}
		memcpy(r->buf, recv_msg_buf.data, length);
	}
	*ierr = 0;
	return;
}

void proActivempi_barrier_(int* job, int* ierr){
	if (*job == myJob) { 
		*ierr = MPI_Barrier(MPI_COMM_WORLD);}
	else{
		*ierr=-1;
		return;
	}
}

void proactivempi_finalize_(int* ierr){
	msg_t send_msg_buf;
	int error;
	int pms;
	
	send_msg_buf.TAG1 = MSG_FINALIZE;
	send_msg_buf.TAG = TAG_S_KEY;
	strcpy(send_msg_buf.data, "");
	pms= sizeof(msg_t) - sizeof(send_msg_buf.TAG) - sizeof(send_msg_buf.data);
	error = msgsnd(C2S_Q_ID, &send_msg_buf, pms+1, 0);
	if (error < 0) {
		if (DEBUG) {fprintf(mslog, "!!! ERROR: msgsnd error\n");}
		perror("ERROR");
		*ierr= -1; return; }
	if (DEBUG) {fflush(mslog);}
	*ierr=0;
	return;
}


void proactivempi_job_(int * job, int* ierr){
	*job=myJob;
	*ierr=0;
}

void proactivempi_allsend_(void * buf, int* cnt, MPI_Datatype* datatype, int* tag, int* idjob, int*ierr)
{ 
	msg_t send_msg_buf;
	int error;
	int pms;
	int length;
	int count = *cnt;
	if (DEBUG) {
		fprintf(mslog, "!!! ProActiveMPI_AllSend \n");}
	send_msg_buf.TAG1 = MSG_ALLSEND;
	send_msg_buf.count = *cnt;
	send_msg_buf.src = myRank ;
	send_msg_buf.dest = -1;
	send_msg_buf.datatype = *datatype;
	send_msg_buf.tag = *tag;
	send_msg_buf.TAG = TAG_S_KEY;
	send_msg_buf.idjob = *idjob;
	
	switch (*datatype){
	case  MPI_CHARACTER: case MPI_BYTE:
		if (DEBUG) {
			fprintf(mslog, "MPI_CHAR Datatype found  \n");}
		length = sizeof(char) * count; 
		break;
	case  MPI_INTEGER:
		if (DEBUG) {
			fprintf(mslog, "MPI_INT Datatype found  \n");}
		length = sizeof(int) * count;
		break;
	case MPI_DOUBLE:
		if (DEBUG) {
			fprintf(mslog, "MPI_DOUBLE Datatype found  \n");}
		length = sizeof(double) * count;
		break; 
	default:
		if (DEBUG) {fprintf(mslog, "!!! BAD DATATYPE \n");}
	*ierr = -1;
	return;
	}
	memcpy(send_msg_buf.data, buf, length);
	pms= sizeof(msg_t) - sizeof(send_msg_buf.TAG) - sizeof(send_msg_buf.data);
	error = msgsnd(C2S_Q_ID, &send_msg_buf, pms+length, 0);
	if (error < 0) {
		if (DEBUG) {fprintf(mslog, "!!! ERROR: msgsnd error\n");}
		perror("ERROR"); 
		*ierr = -1;
		return;  }
	if (DEBUG) {fflush(mslog);}
	*ierr = 0;
	return;
}

void msg_stat(int msgid, struct msqid_ds * msg_info)
{
	int reval;
	reval=msgctl(msgid,IPC_STAT,msg_info);
	if(reval==-1)
	{
		printf( "get msg info error\n");
		return;
	}

	if(reval==-1)
	{
		printf( "set msg info error\n");
		return;
	}

}

int openlog(char *path, int rank){
	char *filename = (char *)malloc(sizeof(char)*256);
	char hostname[MAX_NOM];
	char * nombre = (char *)malloc(sizeof(char)*2);
	sprintf(nombre, "%d", rank);
	gethostname(hostname, MAX_NOM);
	strcpy(filename, path);
	strcat(filename, "/log/mpi_log");
	strcat(filename, "_");
	strcat(filename, hostname);
	strcat(filename, "_");
	strcat(filename, nombre);
	mslog = fopen(filename, "w");
	if(mslog==NULL) return -1;
	else return 0; 
}


// ///////////////////////////////////////////
// ////////// SEMAPHORE FUNCTIONS ////////////
// ///////////////////////////////////////////

/*
 * function: sem_lock. locks the semaphore, for exclusive access to a resource.
 * input: semaphore set ID. output: none.
 */

void sem_lock(int sem_set_id)
{
    /* structure for semaphore operations. */
    struct sembuf sem_op;

    /* wait on the semaphore, unless it's value is non-negative. */
    sem_op.sem_num = 0;
    sem_op.sem_op = -1;
    sem_op.sem_flg = 0;
    semop(sem_set_id, &sem_op, 1);
}

/*
 * function: sem_unlock. un-locks the semaphore. input: semaphore set ID.
 * output: none.
 */
void
sem_unlock(int sem_set_id)
{
    /* structure for semaphore operations. */
    struct sembuf sem_op;

    /* signal the semaphore - increase its value by one. */
    sem_op.sem_num = 0;
    sem_op.sem_op = 1;   /* <-- Comment 3 */
    sem_op.sem_flg = 0;
    semop(sem_set_id, &sem_op, 1);
}

