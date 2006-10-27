/* int_pi2.c
 * This simple program approximates pi by computing pi = integral
 * from 0 to 1 of 4/(1+x*x)dx which is approximated by sum 
 * from k=1 to N of 4 / ((1 + (k-1/2)**2 ).  The only input data
 * required is N.
 * Parallel version number 2:                 (int_pi2.c) 	API
 * Revised: 4/9/93 bbarney
 * Revised: 6/3/93 riordan
 * Revised: 10/11/94 zollweg
 * Converted to MPI: 11/12/94 Xianneng Shen
*/

#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <mpproto.h>			/* API include file */
#include "mpi.h"
#define f(x) ((float)(4.0/(1.0+x*x)))
#define pi ((float)(4.0*atan(1.0)))

MPI_Status status;
main(int argc, char **argv) 
{
float 	err, 
	sum, 
	w,
	x;
int 	i, 
	N, 
	info,
	mynum, 
	nprocs,
	source,
	dest = 0,
	type = 2,
	nbytes = 0,
	EUI_SUCCEED = 0;
void	solicit();

/* All instances call startup routine to get their instance number (mynum) 
*/
MPI_Init(&argc, &argv);
MPI_Comm_rank(MPI_COMM_WORLD, &mynum);
MPI_Comm_size(MPI_COMM_WORLD, &nprocs);
/* Step (1): get a value for N */
solicit (&N, &nprocs, mynum);

/* Step (2): check for exit condition. */
if (N <= 0) {
   printf("node %d left\n", mynum);
   exit(0);
   }

/* Step (3): do the computation in N steps
 * Parallel Version: there are "nprocs" instances participating.  Each
 * instance should do 1/nprocs of the calculation.  Since we want
 * i = 1..n but mynum = 0, 1, 2..., we start off with mynum+1.
 */
while (N > 0) {
   w = 1.0/(float)N;
   sum = 0.0;
   for (i = mynum+1; i <= N; i+=nprocs)
      sum = sum + f(((float)i-0.5)*w);
   sum = sum * w;
   err = sum - pi;

/* Step (4): print the results  
 * Parallel version: collect partial results and let master instance
 * print it.
 */
   if (mynum==0) {
      printf ("host calculated x = %7.5f\n", sum);
      for (i=1; i<nprocs; i++) {
         source = i;
      info = MPI_Recv(&x, 1, MPI_FLOAT, source, type, MPI_COMM_WORLD, &status);
         printf ("host got x = %7.5f\n", x);
         sum=sum+x;
         }
      err = sum - pi;
      printf ("sum, err = %7.5f, %10e\n", sum, err);
      fflush(stdout);
      }
/* Other instances just send their sum and wait for more input */
   else {
     info = MPI_Send(&sum, 1, MPI_FLOAT, dest, type, MPI_COMM_WORLD);
      if (info != 0) {
         printf ("instance no, %d failed to send\n", mynum);
         exit(0);
         }
      printf ("inst %d sent partial sum %7.2f to inst 0\n", mynum, sum);
      fflush(stdout);
      }
   /* get a value of N for the next run */
   solicit (&N, &nprocs, mynum);
   }
   MPI_Finalize();
}

void solicit (pN, pnprocs, mynum)
int *pN, *pnprocs, mynum;
{
/* Get a value for N, the number of intervals in the approximation.
 * (Parallel versions: master instance reads in N and then
 * broadcasts N to all the other instances of the program.)
*/
int source = 0;
   
if (mynum == 0) {
   printf ("Enter number of approximation intervals:(0 to exit)\n");
   scanf ("%d", pN);
   }
MPI_Bcast(pN, 1, MPI_INT, source, MPI_COMM_WORLD);
}
