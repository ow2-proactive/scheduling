#include <stdio.h>
#include "mpi.h"
#include <unistd.h>
#include <string.h>

int main(argc, argv)
int argc;
char **argv;
{
  char hostname[20];
  char hostname2[20];
  int rank, size;
  //int length_name;
  MPI_Init(&argc,&argv);
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  MPI_Comm_size(MPI_COMM_WORLD, &size);
  gethostname(hostname,20);
  //MPI_Get_processor_name(hostname2, &length_name);
  printf("<%s> Hello world! I am %d of %d\n",hostname,rank,size);
  //printf(" proc name %s \n",hostname2);
  sleep(10);
  printf("end\n");
  MPI_Finalize();
  return 0;
}
