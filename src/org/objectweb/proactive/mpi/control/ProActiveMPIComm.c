/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */


#include <mpi.h>
#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <sys/types.h> 
#include <sys/ipc.h> 
#include <sys/msg.h> 
#include <unistd.h> 
#include <signal.h>
#include <sys/time.h>
#include "org_objectweb_proactive_mpi_control_ProActiveMPIComm.h"
#include "ProActiveMPI.h"
#include <errno.h>
#include <sys/sem.h>

#define MAX_NOM 32
#define VERBOSE_MSG 0
#define VERBOSE_STAT 0

//###########################
//####### JNI variables #####
//###########################
jfieldID  itusecID;
jint size, rank, mig_dest, itnum, itsec, itusec;

//###########################
//####### C variables  ######
//###########################
struct msqid_ds bufRecvStat, bufSendStat; 
FILE *mslog;
int C2S_Q_ID, S2C_Q_ID;
msg_t send_msg_buf;
msg_t recv_msg_buf;

//IDs of the semaphore set. 
int sem_set_id_java; 
int sem_set_id_mpi;

//cose the key may change regarding process 
int TAG_S_KEY, TAG_R_KEY;

// for the static proActiveSendRequest
JNIEnv *backup_env;

//###################################
//####### methods declaration  ######
//###################################
void msg_stat(int, struct msqid_ds *);
int msg_get(int);
void exit();
void perror();
void sem_unlock(int sem_set_id);
void sem_lock(int sem_set_id);
char * parseStrng(char * chaine, int iter);
/*
 * Class:     ProActiveMPIComm
 * Method:    Java_ProActiveMPIComm_initRecvQueue
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_objectweb_proactive_mpi_control_ProActiveMPIComm_initRecvQueue
(JNIEnv *env, jobject jthis) {
	
	sem_lock(sem_set_id_java);
	if (VERBOSE_MSG) {fprintf(mslog,"Java_ProActiveMPIComm_initRecvQueue> initialize recv queue! \n"); fflush(mslog);}
	// initialize server->client queue (oa->mpi)
	if ((S2C_Q_ID = msg_get(S2C_KEY)) == -1){
		perror("[NATIVE PROXY] !!! one initRecvQueue exists try to initialize second one");
		if ((S2C_Q_ID = msg_get(S2C02_KEY)) == -1){
			perror("[NATIVE PROXY] !!! ERROR in second initRecvQueue "); 
			return -1;
		}else{
			// update TAG_S_KEY
			TAG_S_KEY=S2C02_KEY;
			if (VERBOSE_MSG) {fprintf(mslog,"Java_ProActiveMPIComm_initRecvQueue> Second recv queue well initialized:: QUEUE ID =  %d! \n", S2C_Q_ID); fflush(mslog);}
		}
	}
	else{
		// update TAG_S_KEY
		TAG_S_KEY=S2C_KEY;
		if (VERBOSE_MSG) {fprintf(mslog,"Java_ProActiveMPIComm_initRecvQueue> First srecv queue well initialized ! :: QUEUE ID =  %d! \n", S2C_Q_ID); fflush(mslog);}
	}
	if (VERBOSE_STAT) {msg_stat(S2C_Q_ID, &bufRecvStat);}
	sem_unlock(sem_set_id_java);
	
	return 0;
}

/*
 * Class:     ProActiveMPIComm
 * Method:    Java_ProActiveMPIComm_initSendQueue
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_objectweb_proactive_mpi_control_ProActiveMPIComm_initSendQueue
(JNIEnv *env, jobject jthis) {
	sem_lock(sem_set_id_java);
	if (VERBOSE_MSG) {fprintf(mslog,"Java_ProActiveMPIComm_initSendQueue> initialize Send queue! \n");fflush(mslog);}	
	//	 initialize client->server queue (mpi->oa)
	if ((C2S_Q_ID = msg_get(C2S_KEY)) == -1){
		perror("[NATIVE PROXY] !!! one initSendQueue exists try to initialize second one"); 
		if ((C2S_Q_ID = msg_get(C2S02_KEY)) == -1){
			perror("[NATIVE PROXY] !!! ERROR in second initSendQueue "); 
			return -1;
		}else{
			// update TAG_R_KEY
			TAG_R_KEY=C2S02_KEY;
			if (VERBOSE_MSG) {fprintf(mslog,"Java_ProActiveMPIComm_initSendQueue> second send queue well initialized !:: QUEUE ID =  %d! \n",C2S_Q_ID); fflush(mslog);}
		}
	}
	else{
		// update TAG_R_KEY
		TAG_R_KEY=C2S_KEY;
		if (VERBOSE_MSG) {fprintf(mslog,"Java_ProActiveMPIComm_initSendQueue> First send queue well initialized !:: QUEUE ID =  %d! \n", C2S_Q_ID); fflush(mslog);}
	}
	if (VERBOSE_STAT) {msg_stat(C2S_Q_ID, &bufSendStat);}
	sem_unlock(sem_set_id_java);
	return 0;
}

/*
 * Class:     ProActiveMPIComm
 * Method:    init
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_objectweb_proactive_mpi_control_ProActiveMPIComm_init
(JNIEnv *env, jobject jthis, jcharArray carr, jint r) {	
	int     rc ;				// return value of system call
	union semun semap;       /* semaphore value, for semctl().     */
	
	if (VERBOSE_MSG) {
		char * nombre = (char *)malloc(sizeof(char)*2);
		char *filename = (char *)malloc(sizeof(char)*256);
		char hostname[MAX_NOM];
		sprintf(nombre, "%d", r);
		gethostname(hostname, MAX_NOM);
		strcpy(filename, (char *) (*env)->GetStringUTFChars(env, carr, JNI_FALSE ));
		strcat(filename, "/log/m_s_log");
		strcat(filename, "_");
		strcat(filename, hostname);
		strcat(filename, "_");
		strcat(filename, nombre);
		mslog = fopen(filename, "w");
		if (mslog==NULL) {perror("[NATIVE PROXY] !!! ERROR in init "); return -1;}
	}
	// REMOVE SEMAPHORES if exist
	if (semctl(sem_set_id_java, 0, IPC_RMID, semap) == -1) {
		if (VERBOSE_MSG){
			fprintf(mslog, "can not remove semphore SEM_ID_JAVA!");}
	}else {
		if (VERBOSE_MSG){
			fprintf(mslog, "semaphore removed SEM_ID_JAVA\n");}
	}
		
	if (semctl(sem_set_id_mpi, 0, IPC_RMID, semap) == -1) {
		if (VERBOSE_MSG){
			fprintf(mslog, "can not remove semphore SEM_ID_MPI!");}
	}else {
		if (VERBOSE_MSG){
			fprintf(mslog, "semaphore removed SEM_ID_MPI\n");}
	}
	
	// create a semaphore set with ID 250, with one semaphore   
    // in it, with access only to the owner.                    
    sem_set_id_java = semget(SEM_ID_JAVA, 1, IPC_CREAT | IPC_EXCL | 0600);
    // if second process
    if (sem_set_id_java == -1) {
    	sem_set_id_java = semget(SEM_ID_JAVA, 1, 0600);
    	if  (sem_set_id_java == -1){
    		perror("[NATIVE PROXY] init: semget sem_set_id_java");
    		exit(1);
    	}
    }// first process
    else{
    	/* initialize the first (and single) semaphore in our set to '1'. */
        semap.val = 1;
        rc = semctl(sem_set_id_java, 0, SETVAL, semap);
        if (rc == -1) {
        	perror("[NATIVE PROXY] init: semctl sem_set_id_java");
        	exit(1);
        }
    }
    
    //  create a semaphore set with ID 350, with one semaphore   
    // in it, with access only to the owner.
    sem_set_id_mpi = semget(SEM_ID_MPI, 1, IPC_CREAT | 0600);
    if  (sem_set_id_mpi == -1){
    	perror("[NATIVE PROXY] init: semget");
    	exit(1);
   	}
    else{
   		/* initialize the first (and single) semaphore in our set to '1'. */
        semap.val = 1;
        rc = semctl(sem_set_id_mpi, 0, SETVAL, semap);
   	}
   
	return 0;
}


/*
 * Class:     ProActiveMPIComm
 * Method:    sendJobNb
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_objectweb_proactive_mpi_control_ProActiveMPIComm_sendJobNb
(JNIEnv *env, jobject jthis, jint jn) {
	int pms = sizeof(msg_t)- sizeof(send_msg_buf.TAG) - sizeof(send_msg_buf.data);
	send_msg_buf.TAG = TAG_S_KEY;
	send_msg_buf.idjob = jn;
	if(msgsnd(S2C_Q_ID, &send_msg_buf, pms+1, 0) < 0){
		if (VERBOSE_MSG) {fprintf(mslog,"Java_ProActiveMPIComm_sendRequest> !!! ERROR WHILE SENDING MSG IN RECV QUEUE \n"); fflush(mslog);}
		return -1;
	}
	return 0;
}


//#########################################################################
//#########################################################################
//#########################################################################
/*
 * Class:     ProActiveMPIComm
 * Method:    Java_ProActiveMPIComm_sendRequest
 * Signature: (I)I
 */
//#########################################################################
//#########################################################################
//#########################################################################

JNIEXPORT jint JNICALL Java_org_objectweb_proactive_mpi_control_ProActiveMPIComm_sendRequest
(JNIEnv *env, jobject jthis, jobject jm_r, jbyteArray joa) {
	int pms = sizeof(msg_t)- sizeof(send_msg_buf.TAG) - sizeof(send_msg_buf.data);
	jfieldID idjobID, TAG1ID, countID, srcID, destID, datatypeID, tagID;
	jclass arrClass;
	jcharArray carr;
	int i;
	jobject jo;
	jmethodID cid;
	int length;
	jbyte * bufByte = (jbyte*) malloc(MSG_SIZE*sizeof(jbyte));
	
	/* Get a reference to MessageRecv class */
	arrClass = (*env)->GetObjectClass(env, jm_r);
	if (arrClass == NULL) 
		return -1;   /* exception thrown */
	if (VERBOSE_MSG){
		fprintf(mslog,"Java_ProActiveMPIComm_sendRequest> Looking for Class [MessageRecv] --> ok \n"); fflush(mslog);
	}
	/* Look for the instance fields in arrClass */
	// idjob
	idjobID = (*env)->GetFieldID(env, arrClass, "jobID","I");
	if (idjobID == NULL) {
		return -1; /* failed to find the field */
	}
	send_msg_buf.idjob = (*env)->GetIntField(env, jm_r, idjobID);
	/* Look for the instance fields in arrClass */
	// TAG1
	TAG1ID = (*env)->GetFieldID(env, arrClass, "TAG1","I");
	if (TAG1ID == NULL) {
		return -1; /* failed to find the field */
	}
	send_msg_buf.TAG1 = (*env)->GetIntField(env, jm_r, TAG1ID);
	//		  count
	countID = (*env)->GetFieldID(env, arrClass, "count","I");
	if (countID == NULL) {
		return -1; /* failed to find the field */
	}
	send_msg_buf.count = (*env)->GetIntField(env, jm_r, countID);
	//			  src
	srcID = (*env)->GetFieldID(env, arrClass, "src","I");
	if (srcID == NULL) {
		return -1; /* failed to find the field */
	}
	send_msg_buf.src = (*env)->GetIntField(env, jm_r, srcID);
//	dest
	destID = (*env)->GetFieldID(env, arrClass, "dest","I");
	if (destID == NULL) {
		return -1; /* failed to find the field */
	}
	send_msg_buf.dest = (*env)->GetIntField(env, jm_r, destID);
	//			  datatype
	datatypeID = (*env)->GetFieldID(env, arrClass, "datatype","I");
	if (datatypeID == NULL) {
		return -1; /* failed to find the field */
	}
	send_msg_buf.datatype = (*env)->GetIntField(env, jm_r, datatypeID);
	
	//			  tag
	tagID = (*env)->GetFieldID(env, arrClass, "tag","I");
	if (tagID == NULL) {
		return -1; /* failed to find the field */
	}
	send_msg_buf.tag = (*env)->GetIntField(env, jm_r, tagID);
	
	switch (send_msg_buf.datatype){
	// ########################################################
	// ### CHAR && UNSIGNED CHAR && BYTE [&& MPI_CHARACTER] ###
	// ########################################################
	case MPI_CHAR: case MPI_UNSIGNED_CHAR: case MPI_BYTE: 	
		length = sizeof(jbyte)*send_msg_buf.count*sizeof(char);
		break;					
		//#########################
		//###  INT &&  UNSIGNED ###
		//#########################
	case MPI_INTEGER: case MPI_INT: case MPI_UNSIGNED:
		length = sizeof(jbyte)*send_msg_buf.count*sizeof(int);
		break;				
		//###############################
		//### SHORT && UNSIGNED_SHORT ###
		//###############################			
	case MPI_SHORT: case MPI_UNSIGNED_SHORT:
		length = sizeof(jbyte)*send_msg_buf.count*sizeof(short);
		break;	
		//#############################
		//### LONG && UNSIGNED_LONG ###
		//#############################			
	case MPI_LONG: case MPI_UNSIGNED_LONG:
		length = sizeof(jbyte)*send_msg_buf.count*sizeof(long);
		break;
		//#############
		//### FLOAT ###
		//#############			
	case MPI_FLOAT:
		length = sizeof(jbyte)*send_msg_buf.count*sizeof(float);
		break;
		//#############
		//### DOUBLE ##
		//#############			
	case MPI_DOUBLE:
		length = sizeof(jbyte)*send_msg_buf.count*sizeof(double);
		break;
		
		//#######################
		//### LONG && DOUBLE  ###
		//#######################			
	case MPI_LONG_DOUBLE:
		length = sizeof(jbyte)*send_msg_buf.count*sizeof(long double);
		break;
		//#######################
		//### LONG && LONG    ###
		//#######################			
	case MPI_LONG_LONG:
		length = sizeof(jbyte)*send_msg_buf.count*sizeof(long long);
		break;	
	default:
		if (VERBOSE_MSG){
			fprintf(mslog,"Java_ProActiveMPIComm_sendRequest>MSG_RECV> !!! ERROR: BAD DATATYPE! \n"); fflush(mslog);
		}
	return -1;
	}

	bufByte = (*env)->GetByteArrayElements(env, joa, 0);	
	
	memcpy(send_msg_buf.data, bufByte, length);
	
	if (VERBOSE_MSG) {fprintf(mslog,"Java_ProActiveMPIComm_sendRequest> sending message in recv queue \n"); fflush(mslog);}
	send_msg_buf.TAG = TAG_S_KEY;
	if(msgsnd(S2C_Q_ID, &send_msg_buf, pms+length, 0) < 0){
		if (VERBOSE_MSG) {fprintf(mslog,"Java_ProActiveMPIComm_sendRequest> !!! ERROR WHILE SENDING MSG IN RECV QUEUE \n"); fflush(mslog);}
		return -1;
	}
	
	if (VERBOSE_STAT) {msg_stat(S2C_Q_ID, &bufRecvStat);}
	return 0;
}


//#########################################################################
//#########################################################################
//#########################################################################
/*
* Class:     ProActiveMPIComm
* Method:    Java_ProActiveMPIComm_proActiveSendRequest
* Signature: (I)I
*/
//#########################################################################
//#########################################################################
//#########################################################################

JNIEXPORT jint JNICALL Java_org_objectweb_proactive_mpi_control_ProActiveMPIComm_proActiveSendRequest
(JNIEnv *env, jobject jthis, jobject jm_r, jbyteArray joa) {
	int pms = sizeof(msg_t)- sizeof(send_msg_buf.TAG) - sizeof(send_msg_buf.data);
	jfieldID idjobID, TAG1ID, countID, srcID, destID, datatypeID, tagID;
	jclass arrClass;
	jcharArray carr;
	int i;
	jobject jo;
	jmethodID cid;
	int length;
	jbyte * bufByte = (jbyte*) malloc(MSG_SIZE*sizeof(jbyte));
	
	/* Get a reference to MessageRecv class */
	arrClass = (*env)->GetObjectClass(env, jm_r);
	if (arrClass == NULL) 
		return -1;   /* exception thrown */
	if (VERBOSE_MSG){
		fprintf(mslog,"Java_ProActiveMPIComm_sendRequest> Looking for Class [MessageRecv] --> ok \n"); fflush(mslog);
	}
	/* Look for the instance fields in arrClass */
	// idjob
	idjobID = (*env)->GetFieldID(env, arrClass, "jobID","I");
	if (idjobID == NULL) {
		return -1; /* failed to find the field */
	}
	send_msg_buf.idjob = (*env)->GetIntField(env, jm_r, idjobID);
	/* Look for the instance fields in arrClass */
	// TAG1
	TAG1ID = (*env)->GetFieldID(env, arrClass, "TAG1","I");
	if (TAG1ID == NULL) {
		return -1; /* failed to find the field */
	}
	send_msg_buf.TAG1 = (*env)->GetIntField(env, jm_r, TAG1ID);
	//		  count
	countID = (*env)->GetFieldID(env, arrClass, "count","I");
	if (countID == NULL) {
		return -1; /* failed to find the field */
	}
	send_msg_buf.count = (*env)->GetIntField(env, jm_r, countID);
	//			  src
	srcID = (*env)->GetFieldID(env, arrClass, "src","I");
	if (srcID == NULL) {
		return -1; /* failed to find the field */
	}
	send_msg_buf.src = (*env)->GetIntField(env, jm_r, srcID);
	//	dest
	destID = (*env)->GetFieldID(env, arrClass, "dest","I");
	if (destID == NULL) {
		return -1; /* failed to find the field */
	}
	send_msg_buf.dest = (*env)->GetIntField(env, jm_r, destID);
	//			  datatype
	datatypeID = (*env)->GetFieldID(env, arrClass, "datatype","I");
	if (datatypeID == NULL) {
		return -1; /* failed to find the field */
	}
	send_msg_buf.datatype = (*env)->GetIntField(env, jm_r, datatypeID);
	
	//			  tag
	tagID = (*env)->GetFieldID(env, arrClass, "tag","I");
	if (tagID == NULL) {
		return -1; /* failed to find the field */
	}
	send_msg_buf.tag = (*env)->GetIntField(env, jm_r, tagID);
	
	switch (send_msg_buf.datatype){
	// ########################################################
	// ### CHAR && UNSIGNED CHAR && BYTE [&& MPI_CHARACTER] ###
	// ########################################################
	case MPI_CHAR: case MPI_UNSIGNED_CHAR: case MPI_BYTE: 	
		length = sizeof(jbyte)*send_msg_buf.count*sizeof(char);
		break;					
		//#########################
		//###  INT &&  UNSIGNED ###
		//#########################
	case MPI_INTEGER: case MPI_INT: case MPI_UNSIGNED:
		length = sizeof(jbyte)*send_msg_buf.count*sizeof(int);
		break;				
		//###############################
		//### SHORT && UNSIGNED_SHORT ###
		//###############################			
	case MPI_SHORT: case MPI_UNSIGNED_SHORT:
		length = sizeof(jbyte)*send_msg_buf.count*sizeof(short);
		break;	
		//#############################
		//### LONG && UNSIGNED_LONG ###
		//#############################			
	case MPI_LONG: case MPI_UNSIGNED_LONG:
		length = sizeof(jbyte)*send_msg_buf.count*sizeof(long);
		break;
		//#############
		//### FLOAT ###
		//#############			
	case MPI_FLOAT:
		length = sizeof(jbyte)*send_msg_buf.count*sizeof(float);
		break;
		//#############
		//### DOUBLE ##
		//#############			
	case MPI_DOUBLE:
		length = sizeof(jbyte)*send_msg_buf.count*sizeof(double);
		break;
		
		//#######################
		//### LONG && DOUBLE  ###
		//#######################			
	case MPI_LONG_DOUBLE:
		length = sizeof(jbyte)*send_msg_buf.count*sizeof(long double);
		break;
		//#######################
		//### LONG && LONG    ###
		//#######################			
	case MPI_LONG_LONG:
		length = sizeof(jbyte)*send_msg_buf.count*sizeof(long long);
		break;	
	default:
		if (VERBOSE_MSG){
			fprintf(mslog,"Java_ProActiveMPIComm_sendRequest>MSG_RECV> !!! ERROR: BAD DATATYPE! \n"); fflush(mslog);
		}
	return -1;
	}

	bufByte = (*env)->GetByteArrayElements(env, joa, 0);	
	
	memcpy(send_msg_buf.data, bufByte, length);
	
	if (VERBOSE_MSG) {fprintf(mslog,"Java_ProActiveMPIComm_sendRequest> sending message in recv queue \n"); fflush(mslog);}
	send_msg_buf.TAG = PROACTIVE_KEY;
	if(msgsnd(S2C_Q_ID, &send_msg_buf, pms+length, 0) < 0){
		if (VERBOSE_MSG) {fprintf(mslog,"Java_ProActiveMPIComm_sendRequest> !!! ERROR WHILE SENDING MSG IN RECV QUEUE \n"); fflush(mslog);}
		return -1;
	}
	
	if (VERBOSE_STAT) {msg_stat(S2C_Q_ID, &bufRecvStat);}
	return 0;
}



//#########################################################################
//#########################################################################
//#########################################################################
/*
 * Class:     ProActiveMPIComm
 * Method:    Java_ProActiveMPIComm_recvRequest
 * Signature: (I)I
 */
//#########################################################################
//#########################################################################
//#########################################################################

JNIEXPORT jbyteArray JNICALL Java_org_objectweb_proactive_mpi_control_ProActiveMPIComm_recvRequest
(JNIEnv *env, jobject jthis, jobject jm_r) {
	int i;
	int pms = sizeof(msg_t)- sizeof(recv_msg_buf.TAG) - sizeof(recv_msg_buf.data);
	int length;
	//jobjectArray ret=NULL;
	jbyteArray ret ;
	jintArray iarr;
	jclass arrClass, newExcCls;
	jobject io;
	int error;
	jthrowable exc;
	char method[64];
	char clazz[64];
	int nb_args;
	jbyte* fake;
	char * parameters;
	// MessageRecv class fields
	
	static jfieldID idjobID, TAG1ID, countID, srcID, destID, datatypeID, tagID, parametersID, methodID, clazzID = NULL;
	// ProActiveMPIComm class field
	jfieldID rankID;
	
	if (VERBOSE_MSG) {fprintf(mslog,"Java_ProActiveMPIComm_recvRequest> Waiting for message in sending queue...:: QUEUE ID=%d \n", C2S_Q_ID); fflush(mslog);}
	error = msgrcv(C2S_Q_ID, &recv_msg_buf, pms + MSG_SIZE, TAG_R_KEY, 0);
	while (error < 0){	
		strerror(errno);
		if (VERBOSE_MSG) {
			fprintf(mslog, "Java_ProActiveMPIComm_recvRequest> !!! ERROR: msgrcv error ERRNO = %d, \n", errno);}
		
		if (errno == EINTR){
			if (VERBOSE_MSG) { fprintf(mslog, "!!! ERRNO = EINTR, \n");}
			error = msgrcv(C2S_Q_ID, &recv_msg_buf, pms + MSG_SIZE, TAG_R_KEY, 0);
		}
		else{
			perror("ERROR");
			return NULL; 
		}
	}
			
		
	
	if (VERBOSE_STAT) {
		msg_stat(C2S_Q_ID, &bufRecvStat);
	}
	// #################################
	// ##### MSG_SEND & MSG_ALLSEND #### 
	// #################################
	if ((recv_msg_buf.TAG1 ==  MSG_SEND) ||
			(recv_msg_buf.TAG1 ==  MSG_ALLSEND) ||
			(recv_msg_buf.TAG1 ==  MSG_SEND_PROACTIVE)){
		/* -----------------------------------------------*/
		/* ----------- update MessageRecv fields    ------*/
		/* -----------------------------------------------*/
		if (idjobID == NULL){
			
			/* Get a reference to MessageRecv class */
			arrClass = (*env)->GetObjectClass(env, jm_r);
			if (arrClass == NULL) 
				return NULL;   /* exception thrown */
			if (VERBOSE_MSG){
				fprintf(mslog,"Java_ProActiveMPIComm_recvRequest>MSG_SEND> Looking for Class [MessageRecv] --> ok \n"); fflush(mslog);
			}		
			/* Look for the instance fields in arrClass */
			// idjob
			idjobID = (*env)->GetFieldID(env, arrClass, "jobID","I");
			if (idjobID == NULL) {
				return NULL; /* failed to find the field */
			}
			TAG1ID = (*env)->GetFieldID(env, arrClass, "TAG1","I");
			if (TAG1ID == NULL) {
				return NULL; /* failed to find the field */
			}
			countID = (*env)->GetFieldID(env, arrClass, "count","I");
			if (countID == NULL) {
				return NULL; /* failed to find the field */
			}
			srcID = (*env)->GetFieldID(env, arrClass, "src","I");
			if (srcID == NULL) {
				return NULL; /* failed to find the field */
			}
			destID = (*env)->GetFieldID(env, arrClass, "dest","I");
			if (destID == NULL) {
				return NULL; /* failed to find the field */
			}
			datatypeID = (*env)->GetFieldID(env, arrClass, "datatype","I");
			if (datatypeID == NULL) {
				return NULL; /* failed to find the field */
			}
			tagID = (*env)->GetFieldID(env, arrClass, "tag","I");
			if (tagID == NULL) {
				return NULL; /* failed to find the field */
			}
			clazzID = (*env)->GetFieldID(env, arrClass, "clazz","Ljava/lang/String;");
			if (clazzID == NULL) {
				return NULL; /* failed to find the field */
			}
			
			methodID = (*env)->GetFieldID(env, arrClass, "method","Ljava/lang/String;");
			if (methodID == NULL) {
				return NULL; /* failed to find the field */
			}
			
			parametersID = (*env)->GetFieldID(env, arrClass, "parameters","Ljava/lang/String;");
			if (parametersID == NULL) {
				return NULL; /* failed to find the field */
			}
			
			// free local reference
			(*env)->DeleteLocalRef(env, arrClass);
		}
		(*env)->SetIntField(env, jm_r, idjobID, (jint) recv_msg_buf.idjob);
		/* Look for the instance fields in arrClass */
		// TAG1
		
		(*env)->SetIntField(env, jm_r, TAG1ID, (jint) recv_msg_buf.TAG1);
		// count
		
		(*env)->SetIntField(env, jm_r, countID, (jint) recv_msg_buf.count);
		// src
		
		(*env)->SetIntField(env, jm_r, srcID, (jint) recv_msg_buf.src);
		// dest
		
		(*env)->SetIntField(env, jm_r, destID, (jint) recv_msg_buf.dest);
		// datatype
		
		(*env)->SetIntField(env, jm_r, datatypeID, (jint) recv_msg_buf.datatype);
		// tag
		(*env)->SetIntField(env, jm_r, tagID, (jint) recv_msg_buf.tag);
		
		if (recv_msg_buf.TAG1 ==  MSG_SEND_PROACTIVE){	
			parameters = (char *) malloc(256);
			if (VERBOSE_MSG){
				fprintf(mslog,"Java_ProActiveMPIComm_recvRequest>MSG_SEND_PROACTIVE> ENTERIN \n"); fflush(mslog);
			}
			sscanf(recv_msg_buf.method, "%s\t%s\t%d", clazz, method, &nb_args );
			
			parameters = parseStrng(recv_msg_buf.method, 3);
			
			(*env)->SetObjectField(env, jm_r, parametersID, (*env)->NewStringUTF(env, parameters));
			
			if (VERBOSE_MSG){
				fprintf(mslog,"Java_ProActiveMPIComm_recvRequest>MSG_SEND_PROACTIVE> NB_ARGS= %d \n", nb_args); fflush(mslog);
			}
			(*env)->SetObjectField(env, jm_r, clazzID, (*env)->NewStringUTF(env, clazz));
			
			(*env)->SetObjectField(env, jm_r, methodID, (*env)->NewStringUTF(env, method));
			if (VERBOSE_MSG){
				fprintf(mslog,"Java_ProActiveMPIComm_recvRequest>MSG_SEND_PROACTIVE> OUTIN \n"); fflush(mslog);
			}
			
		}
		
		//(*env)->DeleteLocalRef(env, arrClass);
		switch (recv_msg_buf.datatype){
		// #####################################
		// ### CHAR && UNSIGNED CHAR && BYTE ###
		// #####################################
		case MPI_CHAR: case MPI_UNSIGNED_CHAR: case MPI_BYTE:
			length = sizeof(jbyte)*recv_msg_buf.count*sizeof(char);
			break;				
			//#########################
			//###  INT &&  UNSIGNED ###
			//#########################
		case MPI_INTEGER: case MPI_INT: case MPI_UNSIGNED:				
			length = sizeof(jbyte)*recv_msg_buf.count*sizeof(int);
			break;				
			//###############################
			//### SHORT && UNSIGNED_SHORT ###
			//###############################			
		case MPI_SHORT: case MPI_UNSIGNED_SHORT:
			length = sizeof(jbyte)*recv_msg_buf.count*sizeof(short);
			break;	
			//#############################
			//### LONG && UNSIGNED_LONG ###
			//#############################			
		case MPI_LONG: case MPI_UNSIGNED_LONG:
			length = sizeof(jbyte)*recv_msg_buf.count*sizeof(long);
			break;
			//#############
			//### FLOAT ###
			//#############			
		case MPI_FLOAT:
			length = sizeof(jbyte)*recv_msg_buf.count*sizeof(float);
			break;
			//#############
			//### DOUBLE ##
			//#############			
		case MPI_DOUBLE:
			length = sizeof(jbyte)*recv_msg_buf.count*sizeof(double);
			break;
			//#######################
			//### LONG && DOUBLE  ###
			//#######################			
		case MPI_LONG_DOUBLE:
			length = sizeof(jbyte)*recv_msg_buf.count*sizeof(long double);
			break;
			//#######################
			//### LONG && LONG    ###
			//#######################			
		case MPI_LONG_LONG:
			length = sizeof(jbyte)*recv_msg_buf.count*sizeof(long long);
			break;
		default:
			if (VERBOSE_MSG){
				fprintf(mslog,"Java_ProActiveMPIComm_recvRequest>MSG_SEND> !!! ERROR: BAD DATATYPE! \n"); fflush(mslog);
			}
		return NULL;
		break;
		}
		
		if (VERBOSE_MSG){
			fprintf(mslog,"Java_ProActiveMPIComm_recvRequest>MSG_SEND> Datatype int nboelements: [%d] \n", recv_msg_buf.count); fflush(mslog);
		}
		ret = (*env)->NewByteArray(env, length);
		(*env)->SetByteArrayRegion(env, ret, 0, length, (jbyte*)recv_msg_buf.data); 
		
		return ret;
	}
	// ##################################
	// ##### MSG_INIT & MSG_FINALIZE #### 
	// ##################################
	else if ((recv_msg_buf.TAG1 == MSG_INIT) || (recv_msg_buf.TAG1 == MSG_FINALIZE)){
		// create a dummy object for initialization case to not return NULL
		arrClass = (*env)->FindClass(env, "java/lang/String");
		if (arrClass == NULL) 
			return NULL;   /* exception thrown */
		if (VERBOSE_MSG){
			fprintf(mslog,"Java_ProActiveMPIComm_recvRequest>MSG_[INIT/FINALIZE]> Class found ...ok \n"); fflush(mslog);
		}
		fake = (jbyte*) malloc(MSG_SIZE*sizeof(jbyte));
		ret = (*env)->NewByteArray(env, MSG_SIZE);
		(*env)->SetByteArrayRegion(env, ret, 0, MSG_SIZE, (jbyte*) fake ); 
		/* Get a reference to ProActiveMPIComm class */
		arrClass = (*env)->GetObjectClass(env, jm_r);
		if (arrClass == NULL) 
			return NULL;   /* exception thrown */
		if (VERBOSE_MSG){
			fprintf(mslog,"Java_ProActiveMPIComm_recvRequest>MSG_[INIT/FINALIZE]> Looking for Class [MessageRecv] --> ok \n"); fflush(mslog);
		}		
		//		 TAG1
		TAG1ID = (*env)->GetFieldID(env, arrClass, "TAG1","I");
		if (TAG1ID == NULL) {
			return NULL; /* failed to find the field */
		}
		(*env)->SetIntField(env, jm_r, TAG1ID, (jint) recv_msg_buf.TAG1);
		//	  ##### MSG_INIT #######
		if (recv_msg_buf.TAG1 == MSG_INIT){
			//		  src
			srcID = (*env)->GetFieldID(env, arrClass, "src","I");
			if (srcID == NULL) {
				return NULL; /* failed to find the field */
			}
			(*env)->SetIntField(env, jm_r, srcID, (jint) recv_msg_buf.src);
		}
		// free local reference
		(*env)->DeleteLocalRef(env, arrClass);
		if (VERBOSE_MSG){
			fprintf(mslog,"Java_ProActiveMPIComm_recvRequest>MSG_[INIT/FINALIZE]> Delete Local Reference --> ok \n"); fflush(mslog);
		}	
	}
	else{
		if (VERBOSE_MSG){
			fprintf(mslog,"Java_ProActiveMPIComm_recvRequest> MSG ?? : [%s] \n",recv_msg_buf.data); fflush(mslog);
		}
	}	
	return ret;	
}


char * parseStrng(char * chaine, int iter){
	int cnt=0;
	while(cnt!=iter){
		cnt++;
		while((*chaine) != '\t'){
			chaine++;
		}
		chaine++;	
	} 
	return chaine;
}

//#########################################################################
//#########################################################################
//#########################################################################
/*
 * Class:     ProActiveMPIComm
 * Method:    closeQueue
 * Signature: (I)I
 */
//#########################################################################
//#########################################################################
//#########################################################################
JNIEXPORT jint JNICALL Java_org_objectweb_proactive_mpi_control_ProActiveMPIComm_closeQueue
(JNIEnv *env, jobject jthis) {
	union semun semap;       /* semaphore value, for semctl().     */
	
	// REMOVE MESSAGE QUEUES
	if (msgctl(C2S_Q_ID, IPC_RMID, NULL) == -1) {
		if (VERBOSE_MSG){
			fprintf(mslog, "can not remove queue C2S_KEY!");}
	}else {
		if (VERBOSE_MSG){
			fprintf(mslog, "message queues removed C2S_KEY\n");}
	}
	
	if (msgctl(S2C_Q_ID, IPC_RMID, NULL) == -1) {
		if (VERBOSE_MSG){
			fprintf(mslog, "can not remove queue S2C_KEY!");}
	}else {
		if (VERBOSE_MSG){
			fprintf(mslog, "message queues removed S2C_KEY\n");}
	}
	// REMOVE SEMAPHORES
	if (semctl(sem_set_id_java, 0, IPC_RMID, semap) == -1) {
		if (VERBOSE_MSG){
			fprintf(mslog, "can not remove semphore SEM_ID_JAVA!");}
	}else{
		if (VERBOSE_MSG){
			fprintf(mslog, "semaphore removed SEM_ID_JAVA\n");}
	}
		
	
	if (semctl(sem_set_id_mpi, 0, IPC_RMID, semap) == -1) {
		if (VERBOSE_MSG){
			fprintf(mslog, "can not remove semphore SEM_ID_MPI!");}
	}else {
		if (VERBOSE_MSG){
			fprintf(mslog, "semaphore removed SEM_ID_MPI\n");}
	}
	if (VERBOSE_MSG){
		fflush(mslog);
	}
	return 0;
}

//#########################################################################
//#########################################################################
//#########################################################################
/*
* Class:     ProActiveMPIComm
* Method:    init
* Signature: (I)I
*/
//#########################################################################
//#########################################################################
//#########################################################################
JNIEXPORT jint JNICALL Java_org_objectweb_proactive_mpi_control_ProActiveMPIComm_closeAllQueues
(JNIEnv *env, jobject jthis) {
	int msqid1, msqid2 ;
	union semun semap;       /* semaphore value, for semctl().     */
	
	// REMOVE MESSAGE QUEUES
	if ((msqid1 = msgget(C2S_KEY, ACCESS_PERM )) == -1) {
		if (VERBOSE_MSG){
			fprintf(mslog, "Cannot open queue C2S_KEY!\n");}
	}else {
		if (msgctl(msqid1, IPC_RMID, NULL) == -1) {
			if (VERBOSE_MSG){
				fprintf(mslog, "can not remove queue C2S_KEY!");}
		}else {
			if (VERBOSE_MSG){
				fprintf(mslog, "message queues removed C2S_KEY\n");}
		}
	}
	if ((msqid1 = msgget(C2S02_KEY, ACCESS_PERM )) == -1) {
		if (VERBOSE_MSG){
			fprintf(mslog, "Cannot open queue C2S02_KEY!\n");}
	}else {
		if (msgctl(msqid1, IPC_RMID, NULL) == -1) {
			if (VERBOSE_MSG){
				fprintf(mslog, "can not remove queue C2S02_KEY!");}
		}else {
			if (VERBOSE_MSG){
				fprintf(mslog, "message queues removed C2S02_KEY\n");}
		}
	}
	if ((msqid2 = msgget(S2C_KEY, ACCESS_PERM )) == -1) {
		if (VERBOSE_MSG){
			fprintf(mslog, "Cannot open queue S2C_KEY!\n");}
	}else {
		if (msgctl(msqid2, IPC_RMID, NULL) == -1) {
			if (VERBOSE_MSG){
				fprintf(mslog, "can not remove queue S2C_KEY!");}
		}else {
			if (VERBOSE_MSG){
				fprintf(mslog, "message queues removed S2C_KEY\n");}
		}
	}
	if ((msqid2 = msgget(S2C02_KEY, ACCESS_PERM )) == -1) {
		if (VERBOSE_MSG){
			fprintf(mslog, "Cannot open queue S2C02_KEY!\n");}
	}else {
		if (msgctl(msqid2, IPC_RMID, NULL) == -1) {
			if (VERBOSE_MSG){
				fprintf(mslog, "can not remove queue S2C02_KEY!");}
		}else {
			if (VERBOSE_MSG){
				fprintf(mslog, "message queues removed S2C02_KEY\n");}
		}
	}

	
	if (VERBOSE_MSG){
		fflush(mslog);
	}
	return 0;
}


int msg_get(int KEY){ 	
	int id;
	if ((id = msgget(KEY, IPC_CREAT| IPC_EXCL | ACCESS_PERM )) == -1){
		if (VERBOSE_MSG){
			fprintf(mslog, "!!!ERROR Init: Cannot open queue! \n");
			fflush(mslog);
		}
	}
	return id;
}


//#########################################################################
//#########################################################################
//#########################################################################
void msg_stat(int msgid, struct msqid_ds * msg_info)
{
	int reval;
	reval=msgctl(msgid,IPC_STAT,msg_info);
	if(reval==-1)
	{
		if (VERBOSE_MSG){
		fprintf(mslog, "get msg info error\n");}
		return;
	}
	fprintf(mslog, "\n");
	fprintf(mslog, "ID of queue is %d \n",msgid);
	fprintf(mslog, "current number of bytes on queue is %d\n",msg_info->msg_cbytes);
	fprintf(mslog, "number of messages in queue is %d\n",msg_info->msg_qnum);
	fprintf(mslog, "max number of bytes on queue is %d\n",msg_info->msg_qbytes);
	fprintf(mslog, "pid of last msgsnd is %d\n",msg_info->msg_lspid);
	fprintf(mslog, "pid of last msgrcv is %d\n",msg_info->msg_lrpid);
	fprintf(mslog, "last msgsnd time is %s", ctime(&(msg_info->msg_stime)));
	fprintf(mslog, "last msgrcv time is %s", ctime(&(msg_info->msg_rtime)));
	fprintf(mslog, "last change time is %s", ctime(&(msg_info->msg_ctime)));
	fprintf(mslog, "msg uid is %d \n",msg_info->msg_perm.uid);
	fprintf(mslog, "msg gid is %d \n",msg_info->msg_perm.gid);
	msg_info->msg_qbytes = MSQ_SIZE;
	reval=msgctl(msgid,IPC_SET,msg_info);
	if(reval==-1)
	{
		fprintf(mslog, "set msg info error\n");
		return;
	}
	fflush(mslog);
}


/////////////////////////////////////////////
////////////SEMAPHORE FUNCTIONS ////////////
/////////////////////////////////////////////

/*
 * function: sem_lock. locks the semaphore, for exclusive access to a resource.
 * input:    semaphore set ID.
 * output:   none.
 */

void sem_lock(int sem_set_id)
{
	/* structure for semaphore operations.   */
	struct sembuf sem_op;
	
	/* wait on the semaphore, unless it's value is non-negative. */
	sem_op.sem_num = 0;
	sem_op.sem_op = -1;
	sem_op.sem_flg = 0;
	semop(sem_set_id, &sem_op, 1);
}

/*
 * function: sem_unlock. un-locks the semaphore.
 * input:    semaphore set ID.
 * output:   none.
 */
void sem_unlock(int sem_set_id)
{
	/* structure for semaphore operations.   */
	struct sembuf sem_op;
	
	/* signal the semaphore - increase its value by one. */
	sem_op.sem_num = 0;
	sem_op.sem_op = 1;   /* <-- Comment 3 */
	sem_op.sem_flg = 0;
	semop(sem_set_id, &sem_op, 1);
}
