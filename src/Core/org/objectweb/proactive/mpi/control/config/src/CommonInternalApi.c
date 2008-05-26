#include "CommonInternalApi.h" 

int myRank=-1;
//FILE* mslog;


FILE * open_debug_log(char *path, int rank, char * prefix) {
	char hostname[MAX_NOM];
	char nombre[2];
	int err = 0;
	sprintf(nombre, "%d", rank);
	gethostname(hostname, MAX_NOM);
	umask(000);
	err = mkdir(path, S_IRWXU | S_IRWXG | S_IRWXO);
	printf("MKDIR RET %d\n", err);
	//	if (err < 0) {
	//		perror("MKDIR failed because");
	//	}

	//TODO possible bug as EEXIST indicate that it could be a file
	strcat(path, "/");
	strcat(path, prefix);
	strcat(path, "_");
	strcat(path, hostname);
	strcat(path, "_");
	strcat(path, nombre);
	printf("PATH %s\n", path);

	return fopen(path, "w");
}

void print_msg_t_(FILE * f, int msg_type, int idjob, int src, int dest, int count, ProActive_Datatype pa_datatype, int tag) {
	fprintf(f, "[MSG_T] msgtype   %d\n", msg_type);
	fprintf(f, "[MSG_T] idjob     %d\n", idjob);
	fprintf(f, "[MSG_T] src       %d\n", src);
	fprintf(f, "[MSG_T] dest      %d, \n", dest);
	fprintf(f, "[MSG_T] count     %d, \n", count);
	fprintf(f, "[MSG_T] datatype  %d, \n", pa_datatype);
	fprintf(f, "[MSG_T] tag       %d, \n", tag);
	fflush(f);
}

void print_msg_t(FILE * f, msg_t * msg) {
	print_msg_t_(f, msg->msg_type, msg->idjob, msg->src, msg->dest, msg->count, msg->pa_datatype, msg->tag);
}

void print_splitted_msg_t(FILE * f, splitted_msg_t * msg) {
	fprintf(f, "[MSG_S] msgtype   %d\n", msg->msg_type);
	fprintf(f, "[MSG_S] data_lg   %d\n", msg->data_length);
	fflush(f);
}

int debug_get_mpi_buffer_length(int count, MPI_Datatype datatype, int byte_size) {
	ProActive_Datatype pa_datatype = type_conversion_MPI_to_proactive(datatype);

	if (pa_datatype == CONV_MPI_PROACTIVE_INT) {
		return sizeof(int) * count;
	} else if (pa_datatype == CONV_MPI_PROACTIVE_DOUBLE) {
		return sizeof(double) * count;
	} else if (pa_datatype == CONV_MPI_PROACTIVE_CHAR) {
		return sizeof(char)* count;
	} else if (pa_datatype == CONV_MPI_PROACTIVE_LONG) {
		return sizeof(long)* count;
	} else {
		printf("ERROR UNKNOW PROACTIVE_DATATYPE %d \n", pa_datatype);
		exit(-4);
	}

	return -1;
}

void init_msg_t(msg_t * msg) {
	int i = 0;
	msg->TAG = 0;
	msg->msg_type = 0;
	msg->idjob = 0;
	msg->count = 0;
	msg->src = 0;
	msg->dest = 0;
	msg->pa_datatype = 0;
	msg->data = NULL;
	msg->tag = 0;

	while (i < MSG_DATA_SIZE) {
		msg->data_backend[i] = 0;
		i++;
	}
	i = 0;
	while (i < MET_SIZE) {
		msg->method[i] = 0;
		i++;
	}
}

void init_splitted_msg_t(splitted_msg_t * msg) {
	int i = 0;
	msg->TAG = 0;
	msg->msg_type = 0;
	msg->data_length = 0;

	while (i < MSG_DATA_SIZE) {
		msg->data[i] = 0;
		i++;
	}
}

int get_payload_size(msg_t * msg) {
	return sizeof(*msg) - sizeof(msg->TAG);
}

int get_data_payload_size(msg_t * msg) {
	int other_info = sizeof(msg_t) - (sizeof(msg->TAG)
			+ sizeof(msg->data_backend));
	return sizeof(msg_t) - other_info;
}

int get_payload_size_splitted_msg(splitted_msg_t * msg) {
	// return the pay load size,
	return sizeof(*msg) - sizeof(msg->TAG);
}

int get_data_payload_size_splitted_msg(splitted_msg_t * msg) {
	// return the data pay load size,
	return (sizeof(*msg) - (sizeof(msg->TAG) + sizeof(msg->msg_type)
			+ sizeof(msg->data_length)));
}

/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+----- TYPE CONVERSION			 -+-----+- */
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

/**
 * 
 *name1 and name2 must be allocated before calling the method.
 * This is needed mainly for efficiency reason. 
 */
/*
 int same_MPI_Datatype(MPI_Datatype datatype1, MPI_Datatype datatype2, char * name1, char * name2) {

 int lg1, lg2, res;
 MPI_Type_get_name(datatype1, name1, &lg1);
 MPI_Type_get_name(datatype2, name2, &lg2);
 res = ((lg1 == lg2) && ((res = strcmp(name1, name2)) == 0)) ? 1 : -1;
 //	printf("[DEBUG_COMMON_API ] same datatype %d,%s and %d,%s ? answer: %d \n", datatype1, name1,  datatype2, name2, res);
 
 return  res;
 }  */

int same_MPI_Datatype(MPI_Datatype datatype1, MPI_Datatype datatype2) {
	return datatype1 == datatype2;
}

/* If return type is MPI_DATATYPE_NULL then it is an error */
MPI_Datatype type_conversion_proactive_to_MPI(ProActive_Datatype datatype) {

	switch (datatype) {
	case CONV_MPI_PROACTIVE_CHAR:
		return MPI_CHAR;

	case CONV_MPI_PROACTIVE_UNSIGNED_CHAR:
		return MPI_UNSIGNED_CHAR;

	case CONV_MPI_PROACTIVE_BYTE:
		return MPI_BYTE;

	case CONV_MPI_PROACTIVE_SHORT:
		return MPI_SHORT;

	case CONV_MPI_PROACTIVE_UNSIGNED_SHORT:
		return MPI_UNSIGNED_SHORT;

	case CONV_MPI_PROACTIVE_INT:
		return MPI_INT;

	case CONV_MPI_PROACTIVE_UNSIGNED:
		return MPI_UNSIGNED;

	case CONV_MPI_PROACTIVE_LONG:
		return MPI_LONG;

	case CONV_MPI_PROACTIVE_UNSIGNED_LONG:
		return MPI_UNSIGNED_LONG;

	case CONV_MPI_PROACTIVE_FLOAT:
		return MPI_FLOAT;

	case CONV_MPI_PROACTIVE_DOUBLE:
		return MPI_DOUBLE;

	case CONV_MPI_PROACTIVE_LONG_DOUBLE:
		return MPI_LONG_DOUBLE;

	case CONV_MPI_PROACTIVE_LONG_LONG_INT:
		return MPI_LONG_LONG;

	default:
		// Unknown data type
		return MPI_DATATYPE_NULL;
	}
}

/* if method return MPI_DATATYPE_NULL then we are not able to convert the mpi datatype to a proactive one */
ProActive_Datatype type_conversion_MPI_to_proactive(MPI_Datatype datatype) {

	if (same_MPI_Datatype(datatype, MPI_CHAR) > 0) {
		return CONV_MPI_PROACTIVE_CHAR;
	}

	if (same_MPI_Datatype(datatype, MPI_UNSIGNED_CHAR) > 0) {
		return CONV_MPI_PROACTIVE_UNSIGNED_CHAR;
	}

	if (same_MPI_Datatype(datatype, MPI_BYTE) > 0) {
		return CONV_MPI_PROACTIVE_BYTE;
	}

	if (same_MPI_Datatype(datatype, MPI_SHORT) > 0) {
		return CONV_MPI_PROACTIVE_SHORT;
	}

	if (same_MPI_Datatype(datatype, MPI_UNSIGNED_SHORT) > 0) {
		return CONV_MPI_PROACTIVE_UNSIGNED_SHORT;
	}

	if (same_MPI_Datatype(datatype, MPI_INT) > 0) {
		return CONV_MPI_PROACTIVE_INT;
	}

	if (same_MPI_Datatype(datatype, MPI_UNSIGNED) > 0) {
		return CONV_MPI_PROACTIVE_UNSIGNED;
	}

	if (same_MPI_Datatype(datatype, MPI_LONG) > 0) {
		return CONV_MPI_PROACTIVE_LONG;
	}

	if (same_MPI_Datatype(datatype, MPI_UNSIGNED_LONG) > 0) {
		return CONV_MPI_PROACTIVE_UNSIGNED_LONG;
	}

	if (same_MPI_Datatype(datatype, MPI_FLOAT) > 0) {
		return CONV_MPI_PROACTIVE_FLOAT;
	}

	if (same_MPI_Datatype(datatype, MPI_DOUBLE) > 0) {
		return CONV_MPI_PROACTIVE_DOUBLE;
	}

	if (same_MPI_Datatype(datatype, MPI_LONG_DOUBLE) > 0) {
		return CONV_MPI_PROACTIVE_LONG_DOUBLE;
	}

	if (same_MPI_Datatype(datatype, MPI_LONG_LONG) > 0) {
		return CONV_MPI_PROACTIVE_LONG_LONG_INT;
	}

	return CONV_MPI_PROACTIVE_NULL;
}
/*
 ProActive_Datatype type_conversion_MPI_to_proactive (MPI_Datatype datatype) {

 char name1 [MPI_MAX_OBJECT_NAME];
 char name2 [MPI_MAX_OBJECT_NAME];
 
 if (same_MPI_Datatype(datatype, MPI_CHAR, name1, name2) == 1) {
 return CONV_MPI_PROACTIVE_CHAR;
 }

 if (same_MPI_Datatype(datatype, MPI_UNSIGNED_CHAR, name1, name2) == 1) {
 return CONV_MPI_PROACTIVE_UNSIGNED_CHAR;
 }
 
 if (same_MPI_Datatype(datatype, MPI_BYTE, name1, name2) == 1) {
 return CONV_MPI_PROACTIVE_BYTE;
 }
 
 if (same_MPI_Datatype(datatype, MPI_SHORT, name1, name2) == 1) {
 return CONV_MPI_PROACTIVE_SHORT;
 }
 
 if (same_MPI_Datatype(datatype, MPI_UNSIGNED_SHORT, name1, name2) == 1) {
 return CONV_MPI_PROACTIVE_UNSIGNED_SHORT;
 }
 
 if (same_MPI_Datatype(datatype, MPI_INT, name1, name2) == 1) {
 return CONV_MPI_PROACTIVE_INT;
 }

 if (same_MPI_Datatype(datatype, MPI_UNSIGNED, name1, name2) == 1) {
 return CONV_MPI_PROACTIVE_UNSIGNED;
 }
 
 if (same_MPI_Datatype(datatype, MPI_LONG, name1, name2) == 1) {
 return CONV_MPI_PROACTIVE_LONG;
 }

 if (same_MPI_Datatype(datatype, MPI_UNSIGNED_LONG, name1, name2) == 1) {
 return CONV_MPI_PROACTIVE_UNSIGNED_LONG;
 }

 if (same_MPI_Datatype(datatype, MPI_FLOAT, name1, name2) == 1) {
 return CONV_MPI_PROACTIVE_FLOAT;
 }

 if (same_MPI_Datatype(datatype, MPI_DOUBLE, name1, name2) == 1) {
 return CONV_MPI_PROACTIVE_DOUBLE;
 }
 
 if (same_MPI_Datatype(datatype, MPI_LONG_DOUBLE, name1, name2) == 1) {
 return CONV_MPI_PROACTIVE_LONG_DOUBLE;
 }
 
 if (same_MPI_Datatype(datatype, MPI_LONG_LONG, name1, name2) == 1) {
 return CONV_MPI_PROACTIVE_LONG_LONG_INT;
 }
 
 return CONV_MPI_PROACTIVE_NULL;
 }*/

/*
 int get_mpi_buffer_length(int count, MPI_Datatype datatype, int byte_size) {
 int type_length = 0;

 int res = MPI_Type_size(datatype, &type_length);
 
 if (res == MPI_SUCCESS)
 return byte_size * count * type_length;
 
 if (res == MPI_ERR_TYPE) {
 if (DEBUG_COMMON_API) {
 fprintf(mslog, "ProActiveMPIComm: get_buffer_length: MPI_Type_size: Invalid datatype argument %d", MPI_ERR_TYPE);
 }
 }
 
 if (res == MPI_ERR_ARG) {
 if (DEBUG_COMMON_API) {
 fprintf(mslog, "ProActiveMPIComm: get_buffer_length: MPI_Type_size: Invalid argument %d", MPI_ERR_ARG);
 }
 }
 
 return res;
 }
 */
int get_proactive_buffer_length(int count, ProActive_Datatype datatype) {
	MPI_Datatype mpi_datatype = type_conversion_proactive_to_MPI(datatype);
	return debug_get_mpi_buffer_length(count, mpi_datatype, sizeof(char));
}

void free_msg_t(msg_t * recv_msg_buf) {
	// We no more need the message	
	free_msg_t_data_buffer(recv_msg_buf);

	free(recv_msg_buf);
}

void free_msg_t_data_buffer(msg_t * recv_msg_buf) {
	if (is_splittable_msg(recv_msg_buf)) {
		free(recv_msg_buf->data);
	}
}

int is_splittable_msg(msg_t * recv_msg_buf) {
	return (get_data_payload_size(recv_msg_buf) < get_proactive_buffer_length(
			recv_msg_buf->count, recv_msg_buf->pa_datatype));
}

/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+----- RECEIVE MESSAGES 			 -+-----+- */
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

int recv_raw_msg_from_ipc_queue(int qid, long msg_type, void * recv_msg_buf,
		int size, int no_wait, int * ret_code) {
	int error = 0;
	int flag = no_wait ? IPC_NOWAIT : 0;

	do {
		error = msgrcv(qid, recv_msg_buf, size, msg_type, flag);

		if (error < 0) {
			*ret_code = errno;
			if ((errno != ENOMSG) && (DEBUG_COMMON_API)) {
					fprintf(
							mslog,
							"[recv_raw_msg_from_ipc_queue] !!! ERROR: msgrcv error ERRNO = %d, \n",
							errno);
					fprintf(
							mslog,
							"[recv_raw_msg_from_ipc_queue] ERROR dUmp: qid %d, msg_type %ld, size %d\n",
							qid, msg_type, size);
					fprintf(mslog,
							"[recv_raw_msg_from_ipc_queue] PERROR STR :: %s",
							strerror(errno));
			}
			// we do not display error message if errno == ENOMSG
			// as it means no_wait = true and it's not a runtime error.
			// however we still return error < 0 as it indicates to upper layer
			// no message has been received.
		}
	} while (errno == EINTR); // iterate if we've been interrupted

	return error;
}

int recv_ipc_message(int qid, long int tag, msg_t * recv_msg, int no_wait,
		int * ret_code) {
	int err = recv_raw_msg_from_ipc_queue(qid, tag, recv_msg, get_payload_size(recv_msg), no_wait, ret_code);
	
	if (err < 0) {
		return err;
	}
	
	// we have received a message 
	if (DEBUG_COMMON_API) {
		fprintf(mslog, "[recv_ipc_message] Received message \n");
		print_msg_t(mslog, recv_msg);
	}

	if (recv_msg->msg_type == MSG_SEND_SPLIT_BEGIN) {
		// the message is splitted in several part we need to get remaining parts from the queue.
		// WARNING whether we are in no_wait mode or not, we must always finish to read a splitted message !!!
		int data_size_to_recv = get_proactive_buffer_length(recv_msg->count,
				recv_msg->pa_datatype);
		int receive = 1;
		char * data_ptr_save;
		char * data_ptr;
		splitted_msg_t recv_splitted_msg_buf;

		if (DEBUG_COMMON_API) {
			fprintf(mslog,
					"[recv_ipc_message] [BEGIN] Receiving splitted message \n");
		}

		if ((data_ptr_save = (char *) calloc(data_size_to_recv, sizeof(char)))
				< 0) { // when do we free this one ??
			perror("MALLOC ERROR");
			return -1; //TODO errno ?
		}

		data_ptr = data_ptr_save;

		while (receive == 1) {
			if ((err = recv_raw_msg_from_ipc_queue(qid, tag,
					&recv_splitted_msg_buf,
					get_payload_size_splitted_msg(&recv_splitted_msg_buf), 0,
					ret_code)) < 0) {
				return err;
			}

			// memcpy retrieved data message
			memcpy(data_ptr, recv_splitted_msg_buf.data,
					recv_splitted_msg_buf.data_length);

			// shifting data pointer
			data_ptr += recv_splitted_msg_buf.data_length;

			// we receive 'data_length' bytes, we shift the pointer for msg to come.

			if (recv_splitted_msg_buf.msg_type == MSG_SEND_SPLIT_END) {
				// end of splitted message
				receive = 0;
				// We build the merged message
				recv_msg->msg_type = MSG_SEND;
				recv_msg->count = recv_msg->count;
				// updating data ptr to the concatenated buffer
				recv_msg->data = data_ptr_save;
				//TODO check updated
				if (DEBUG_COMMON_API) {
					fprintf(mslog,
							"[recv_ipc_message] [END] Receiving splitted message DUMP :\n");
					print_msg_t(mslog, recv_msg);
				}
			}
		}
	} else {
		// updating data ptr
		recv_msg->data = recv_msg->data_backend;
	}

	return 0;
}

/*---+----+-----+----+----+-----+----+----+-----+----+-*/
/*---+----+----- SEND MESSAGES 			     -+-----+- */
/*---+----+-----+----+----+-----+----+----+-----+----+-*/

int send_raw_msg_to_ipc_queue(int id, void * send_msg_buf, int size) {

	int error = msgsnd(id, send_msg_buf, size, 0);

	if (error < 0) {
		if (DEBUG_COMMON_API) {
			fprintf(mslog,
					"[send_raw_msg_to_ipc_queue] !!! ERROR: msgsnd error\n");
		}
		perror("[send_raw_msg_to_ipc_queue] ERROR ");
		return -1;
	}
	if (DEBUG_COMMON_API) {
		fflush(mslog);
	}
	return 0;
}

int populate_msg(msg_t * send_msg_buf, int msg_type, long int TAG, void * buf,
		int count, MPI_Datatype datatype, int src, int dest, int tag, int idjob) {
	int length;
	// assert length < MSG_DATA_SIZE
	send_msg_buf->msg_type = msg_type;
	send_msg_buf->count = count;
	send_msg_buf->src = src;
	send_msg_buf->dest = dest;
	send_msg_buf->pa_datatype = type_conversion_MPI_to_proactive(datatype);
	send_msg_buf->tag = tag;
	send_msg_buf->TAG = TAG;
	send_msg_buf->idjob = idjob;
	send_msg_buf->data = NULL;
	length = debug_get_mpi_buffer_length(count, datatype, sizeof(char));

	if (length < 0) {
		if (DEBUG_COMMON_API) {
			fprintf(mslog, "[populate_msg] [ERROR] !!! BAD DATATYPE \n");
		}
		return -1;
	} else if (length > get_data_payload_size(send_msg_buf)) {
		// this message will have to be split because it oversize message buffer capacity.
		if (DEBUG_COMMON_API) {
			fprintf(mslog,
					"[populate_msg] Populating a message to be splitted \n");
		}
		send_msg_buf->data = (char *) buf;
	} else {
		memcpy(send_msg_buf->data_backend, buf, length);
		send_msg_buf->data = send_msg_buf->data_backend;
	}
	
	return 0;
}

int populate_splitted_message(splitted_msg_t * send_msg_buf, int msg_type,
		long int TAG, void * buf, int length) {
	send_msg_buf->msg_type = msg_type;
	send_msg_buf->data_length = length;
	send_msg_buf->TAG = TAG;

	memcpy(send_msg_buf->data, buf, length);

	return 0;
}

int send_ipc_message(int qid, msg_t * send_msg_buf) {

	int pms = get_payload_size(send_msg_buf);

	if (DEBUG_COMMON_API) {
		fprintf(mslog, "[send_ipc_message] Sending message ... \n");
		print_msg_t(mslog, send_msg_buf);
	}
	return send_raw_msg_to_ipc_queue(qid, send_msg_buf, pms);
}

int send_splitted_message(int qid, msg_t * send_msg_buf) {

	/*, int msg_type, long int TAG, void * buf, int count, MPI_Datatype datatype, 
	 int src, int dest, int tag, int idjob) {*/
	splitted_msg_t splitted_msg;
	int remaining_elt_to_send = 0;
	int length_to_send = 0;
	int datatype_size_unit = 0;
	int max_elt_in_msg = 0;
	int nb_elem_to_send = 0;
	int msg_type = -1;
	int err = 0;
	int first_run = 1;

	if (DEBUG_STMT) {
		// clear buffers in debug mode to avoid valgrind warnings
		init_splitted_msg_t(&splitted_msg);
	}

	// get size of one element
	datatype_size_unit = get_proactive_buffer_length(1,
			send_msg_buf->pa_datatype);

	length_to_send = send_msg_buf->count * datatype_size_unit;
	//	MPI_Type_size(datatype, &length_to_send);

	remaining_elt_to_send = send_msg_buf->count;
	max_elt_in_msg = (get_data_payload_size_splitted_msg(&splitted_msg)
			/ datatype_size_unit);

	// data to send is pointing a buffer allocated outside of the msg_t
	char * buffer_ind = send_msg_buf->data;

	/* void * buf, int count, MPI_Datatype datatype, int dest, int tag, int idjob */

	while (remaining_elt_to_send > 0) {
		// new message size
		//		nb_elem_to_send  = (remaining_elt_to_send % max_elt_in_msg);

		if (first_run == 1) {
			// We send the first part of the message which is a regular msg_t
			send_msg_buf->msg_type = MSG_SEND_SPLIT_BEGIN;
			err = send_ipc_message(qid, send_msg_buf);
			if (err < 0) {
				return err;
			}
			first_run = 0;
		} else {

			if ((remaining_elt_to_send - max_elt_in_msg) < 0) {
				nb_elem_to_send = remaining_elt_to_send % max_elt_in_msg;
				// Add last data slice tag to the buffer
				msg_type = MSG_SEND_SPLIT_END;
			} else {
				nb_elem_to_send = max_elt_in_msg;
				msg_type = MSG_SEND_SPLIT;
			}

			if (DEBUG_COMMON_API) {
				fprintf(mslog, "DEBUG length_to_send %d\n", length_to_send);
				fprintf(mslog, "DEBUG remaining_elt_to_send %d\n",
						remaining_elt_to_send);
				fprintf(mslog, "DEBUG datatype_size_unit %d\n",
						datatype_size_unit);
				fprintf(mslog, "DEBUG max_elt_in_msg %d\n", max_elt_in_msg);
				fprintf(mslog, "DEBUG nb_elem_to_send %d\n", nb_elem_to_send);
				fprintf(mslog, "DEBUG data_payload %d\n",
						get_data_payload_size_splitted_msg(&splitted_msg));
			}

			if ((err = populate_splitted_message(&splitted_msg, msg_type,
					send_msg_buf->TAG /*TAG*/, buffer_ind, nb_elem_to_send
							* datatype_size_unit)) < 0) {
				return err;
			}
			if (DEBUG_COMMON_API) {
				fprintf(mslog, "Sending splitted message \n");
				print_splitted_msg_t(mslog, &splitted_msg);
			}
			if ((err = send_raw_msg_to_ipc_queue(qid, &splitted_msg,
					get_payload_size_splitted_msg(&splitted_msg))) < 0) {
				return err;
			}

			remaining_elt_to_send -= nb_elem_to_send;
			buffer_ind += (nb_elem_to_send * datatype_size_unit);
		}
	}

	return err;
}

int send_to_ipc(int qid, int msg_type, long int TAG, void * buf, int count,
		MPI_Datatype datatype, int src, int dest, int tag, int idjob) {
	msg_t send_msg_buf; //TODO avoid instantiation here

	if (DEBUG_STMT) {
		// clear buffers in debug mode to avoid valgrind warnings
		init_msg_t(&send_msg_buf);
	}

	int ret = populate_msg(&send_msg_buf, msg_type, TAG, buf, count, datatype,
			src, dest, tag, idjob);
	if (ret < 0) {
		return ret;
	}

	if (is_splittable_msg(&send_msg_buf)) {
		if (DEBUG_COMMON_API) {
			fprintf(mslog, "Sending splitted message\n");
		}
		return send_splitted_message(qid, &send_msg_buf);
	} else {
		if (DEBUG_COMMON_API) {
			fprintf(mslog, "Sending regular message\n");
		}
		return send_ipc_message(qid, &send_msg_buf);
	}
}

/////////////////////////////////////////////
////////////SEMAPHORE FUNCTIONS ////////////
/////////////////////////////////////////////

/*
 * function: sem_lock. locks the semaphore, for exclusive access to a resource.
 * input:    semaphore set ID.
 * output:   none.
 */

void sem_lock(int sem_set_id) {
	/* structure for semaphore operations.   */
	struct sembuf sem_op;
	int ret = -1;

	/* wait on the semaphore, unless it's value is non-negative. */
	sem_op.sem_num = 0;
	sem_op.sem_op = -1;
	sem_op.sem_flg = 0;

	if ((ret = semop(sem_set_id, &sem_op, 1)) < 0) {
		perror("sem_lock error ");
	}
}

/*
 * function: sem_unlock. un-locks the semaphore.
 * input:    semaphore set ID.
 * output:   none.
 */
void sem_unlock(int sem_set_id) {
	/* structure for semaphore operations.   */
	struct sembuf sem_op;
	int ret = -1;

	/* signal the semaphore - increase its value by one. */
	sem_op.sem_num = 0;
	sem_op.sem_op = 1; /* <-- Comment 3 */
	sem_op.sem_flg = 0;

	if ((ret = semop(sem_set_id, &sem_op, 1)) < 0) {
		perror("sem_unlock error ");
	}

}
