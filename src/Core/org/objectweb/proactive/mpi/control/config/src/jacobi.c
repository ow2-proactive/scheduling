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
#include <stdio.h>
#include <math.h>
#include "mpi.h"
#include "ProActiveMPI.h"
#include <time.h>
/* This example handles a 1600x1600 mesh, on 4 processors only. */
#define maxn 12
#define size 12
#define JOB_ZERO 0
#define JOB_ONE  1
#define NB_ITER  100

int main( argc, argv )
int argc;
char **argv;
{
	int        rank, value, errcnt, toterr, i, j, itcnt, idjob, nb_proc;
	int        i_first, i_last;
	MPI_Status status;
	double     diffnorm, gdiffnorm;
	double     xlocal[(size/3)+2][maxn];
	double     xnew[(size/3)+1+2][maxn];
	double 	   test[maxn];
	int initValue;
	int error;
	double t_0, t_end, t_00, t_01, t_10, t_11;
	char processor_name[MPI_MAX_PROCESSOR_NAME];
	int  namelen;
	double waitForRecv=0.0;
	int k;
	ProActiveMPI_Request request;
	int flag;
	
	
	MPI_Init( &argc, &argv );
	MPI_Comm_rank( MPI_COMM_WORLD, &rank );
	MPI_Comm_size( MPI_COMM_WORLD, &nb_proc );
	MPI_Get_processor_name(processor_name,&namelen);
	
	
	error = ProActiveMPI_Init(rank);
	if (error < 0){
		printf("[MPI] !!! Error ProActiveMPI init \n");
		MPI_Abort( MPI_COMM_WORLD, 1 );
	}
	
	// get the process jon number
	ProActiveMPI_Job(&idjob);
	if (nb_proc != 4) MPI_Abort( MPI_COMM_WORLD, 1 );
	
	if (rank==0) t_0 = MPI_Wtime();
	
	// printf("I am rank %d job %d \n", rank, idjob );
	/* xlocal[][0] is lower ghostpoints, xlocal[][maxn+2] is upper */
	
	/*
	 * Note that top and bottom processes have one less row of interior points
	 */
	i_first = 1;
	i_last = size/nb_proc;
	
	if ((rank == 0) && (idjob==JOB_ZERO)) i_first++;
	if ((rank == nb_proc - 1) && (idjob==JOB_ONE)) i_last--;
	
	// initialization
	if (idjob==JOB_ZERO) initValue=rank;
	else {initValue=nb_proc+rank;} 
	
		
	/* Fill the data as specified */
	for (i=1; i<=size/nb_proc; i++)
		for (j=0; j<maxn; j++)
			xlocal[i][j] = initValue;
	for (j=0; j<maxn; j++) {
		xlocal[i_first-1][j] = -1;
		xlocal[i_last+1][j] = -1;
	}
	
	itcnt = 0;
	do {
	
		/*----+----+----+----+----+----+ MPI COMMS +----+----+----+----+----+----+*/
		/* Send up unless I'm at the top, then receive from below */
		/* Note the use of xlocal[i] for &xlocal[i][0] */
		if (rank < nb_proc - 1) 
			MPI_Send( xlocal[size/nb_proc], maxn, MPI_DOUBLE, rank + 1, 0, 
					MPI_COMM_WORLD );
		
		if (rank > 0)			
			MPI_Recv( xlocal[0], maxn, MPI_DOUBLE, rank - 1, 0, 
					MPI_COMM_WORLD, &status );
	
		/*----+----+----+----+----+----+ PROACTIVE COMMS +----+----+----+----+----+----+*/
		if ((rank == nb_proc - 1) && (idjob==JOB_ZERO)){ 
			error = ProActiveMPI_Send(xlocal[size/nb_proc], maxn, MPI_DOUBLE, 0, 0, JOB_ONE);
			if (error < 0){
				printf("[MPI] !!! Error ProActiveMPI send #1-0 \n");}
		}
		
		if ((rank == 0) && (idjob==JOB_ONE)) {
			t_00 = MPI_Wtime();
			error = ProActiveMPI_Recv(xlocal[0], maxn, MPI_DOUBLE, nb_proc - 1, 0, JOB_ZERO);
			t_01 = MPI_Wtime();
			waitForRecv += t_01 - t_00;
			if (error < 0){
				printf("[MPI] !!! Error ProActiveMPI recv #0-1 \n");}
			
		}
		
		
		/*----+----+----+----+----+----+ MPI COMMS +----+----+----+----+----+----+*/
		/* Send down unless I'm at the bottom */
		if (rank > 0) 
			MPI_Send( xlocal[1], maxn, MPI_DOUBLE, rank - 1, 1, 
					MPI_COMM_WORLD );
		
		if (rank < nb_proc - 1) 
			MPI_Recv( xlocal[size/nb_proc+1], maxn, MPI_DOUBLE, rank + 1, 1, 
					MPI_COMM_WORLD, &status );
		
		
		/*----+----+----+----+----+----+ PROACTIVE COMMS +----+----+----+----+----+----+*/
		if ((rank == 0) && (idjob==JOB_ONE)){
			error = ProActiveMPI_Send(xlocal[1], maxn, MPI_DOUBLE, nb_proc - 1, 1, JOB_ZERO);
			if (error < 0){
				printf("[MPI] !!! Error ProActiveMPI send #0-1 \n");}
			
		}
		
		if ((rank == nb_proc - 1) && (idjob==JOB_ZERO)) {
			t_00 = MPI_Wtime();
			error = ProActiveMPI_Recv(xlocal[size/nb_proc+1], maxn, MPI_DOUBLE, 0, 1, JOB_ONE);
			t_01 = MPI_Wtime();
			if (error < 0){
				printf("[MPI] !!! Error ProActiveMPI recv #1-0 \n");}
			waitForRecv += t_01 - t_00;
			
		}
		/*----+----+----+----+----+----+ COMPUTATION +----+----+----+----+----+----+*/
		/* Compute new values (but not on boundary) */
		itcnt ++;
		diffnorm = 0.0;
		for (i=i_first; i<=i_last; i++) 
			for (j=1; j<maxn-1; j++) {
				xnew[i][j] = (xlocal[i][j+1] + xlocal[i][j-1] +
						xlocal[i+1][j] + xlocal[i-1][j]) / 4.0;
				diffnorm += (xnew[i][j] - xlocal[i][j]) * 
				(xnew[i][j] - xlocal[i][j]);
			}
		/* Only transfer the interior points */
		for (i=i_first; i<=i_last; i++) 
			for (j=1; j<maxn-1; j++) 
				xlocal[i][j] = xnew[i][j];
		
// // MPI_Allreduce( &diffnorm, &gdiffnorm, 1, MPI_DOUBLE, MPI_SUM,
// // MPI_COMM_WORLD );
// // gdiffnorm = sqrt( gdiffnorm );
		if (rank == 0) printf( "[MPI] At iteration %d, job %d \n", itcnt, idjob );
	} while (itcnt < NB_ITER);
	
	printf("[MPI] Rank: %d Job: %d \n",rank, idjob );
	for (i=1; i<(size/16); i++){
		printf("[");
		for (j=0; j<maxn; j++)
			printf( "%f ",xlocal[i][j]);
		printf("] \n");
	}
	
	if (rank==0) {
		t_end = MPI_Wtime();
		printf("############### Execution time ###############\n####\n");
		printf("#### %d iter #  %.2lf sec \n####\n", NB_ITER,  (double)
				t_end-t_0);
		printf("####################################################\n");
	}
	// if ((rank==nb_proc-1) && (idjob==JOB_ZERO)){
	// printf("############### Waiting time ###############\n####\n");
	// printf("#### %d iter # job %d : wt %.2lf sec \n####\n", NB_ITER, idjob,
	// waitForRecv);
	// printf("####################################################\n");
	// }
	// if ((rank==0) && (idjob==JOB_ONE)){
// printf("############### Waiting time ###############\n####\n");
	// printf("#### %d iter # job %d : wt %.2lf sec \n####\n", NB_ITER, idjob,
	// waitFor);
	// printf("####################################################\n");
	// }
	
	ProActiveMPI_Finalize();
	MPI_Finalize( );
	return 0;
}



