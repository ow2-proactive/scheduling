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
#include "mpi.h"
#include "ProActiveMPI.h"
#include <time.h>

/* This example handles a 1680x1680 mesh, on 2 clusters with 16 nodes (2 ppn) for each  */
#define maxn 1680
#define size 840
#define JOB_ZERO 0
#define JOB_ONE  1
#define NB_ITER  10000

int main( argc, argv )
int argc;
char **argv;
{
	int        rank, value, errcnt, toterr, i, j, itcnt, idjob, nb_proc;
	int        i_first, i_last;
	MPI_Status status;
	double     diffnorm, gdiffnorm;
	double     xlocal[(size/3)+2][maxn];
	double     xnew[(size/3)+3][maxn];
	double 	   test[maxn];
	int initValue;
	int error;
	char processor_name[MPI_MAX_PROCESSOR_NAME];
	int  namelen;
	

	
	
	MPI_Init( &argc, &argv );
	MPI_Comm_rank( MPI_COMM_WORLD, &rank );
	MPI_Comm_size( MPI_COMM_WORLD, &nb_proc );
	MPI_Get_processor_name(processor_name,&namelen);
	
	
	error = ProActiveMPI_Init(rank);
	if (error < 0){
		printf("[MPI] !!! Error ProActiveMPI init \n");
		MPI_Abort( MPI_COMM_WORLD, 1 );
	}
	
	// get this process job ID
	ProActiveMPI_Job(&idjob);
	if (nb_proc != 16) MPI_Abort( MPI_COMM_WORLD, 1 );
	
	/* xlocal[][0] is lower ghostpoints, xlocal[][maxn+2] is upper */
	
	/*
	 * Note that top and bottom processes have one less row of interior points
	 */
	i_first = 1;
	i_last = size/nb_proc;
	
	if ((rank == 0) && (idjob ==J OB_ZERO)) i_first++;
	if ((rank == nb_proc - 1) && (idjob == JOB_ONE)) i_last--;
	
	// initialization
	if (idjob==JOB_ZERO) initValue=rank;
	else {initValue = nb_proc+rank;} 
	
		
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
		if ((rank == nb_proc - 1) && (idjob == JOB_ZERO)){ 
			error = ProActiveMPI_Send(xlocal[size/nb_proc], maxn, MPI_DOUBLE, 0, 0, JOB_ONE);
			if (error < 0){
				printf("[MPI] !!! Error ProActiveMPI send #15/0 -> #0/1 \n");}
		}
		
		if ((rank == 0) && (idjob==JOB_ONE)) {
			error = ProActiveMPI_Recv(xlocal[0], maxn, MPI_DOUBLE, nb_proc - 1, 0, JOB_ZERO);
			if (error < 0){
				printf("[MPI] !!! Error ProActiveMPI recv #0/1 <- #15/0 \n");}
			
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
				printf("[MPI] !!! Error ProActiveMPI send #0/1 -> #15/0 \n");}
			
		}
		
		if ((rank == nb_proc - 1) && (idjob==JOB_ZERO)) {
			t_00 = MPI_Wtime();
			error = ProActiveMPI_Recv(xlocal[size/nb_proc+1], maxn, MPI_DOUBLE, 0, 1, JOB_ONE);
			t_01 = MPI_Wtime();
			if (error < 0){
				printf("[MPI] !!! Error ProActiveMPI recv  #15/0 <- #0/1 \n");}
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
		
		if (rank == 0) printf( "[MPI] At iteration %d, job %d \n", itcnt, idjob );
	} while (itcnt < NB_ITER);
	
	printf("[MPI] Rank: %d Job: %d \n",rank, idjob );
	for (i=1; i<(size/16); i++){
		printf("[");
		for (j=0; j<maxn; j++)
			printf( "%f ",xlocal[i][j]);
		printf("] \n");
	}
	
	ProActiveMPI_Finalize();
	MPI_Finalize( );
	return 0;
}



