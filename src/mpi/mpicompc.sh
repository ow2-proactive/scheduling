#
# PROJECT NAME:
#   PAPI (ProActive Parallel Interface)
# FILE NAME:
#   mpicomp
# DEPENDENCY FILE(S):
#   -
#
# DATE:
#   10-23-03
# UPDATE:
#   08-04-04
# REVISION NUMBER:
PAPI_MPICOMP_REV=1AA-040804-00
# CHECKED:
#   Yes
#
# LANGUAGE:
#   BASH
# COMPILATION LINE:
#   ConfigFileName
# ENVIRONMENT CONFIGURATION:
#   -
#
# DESCRIPTION:
#   Compile the C part of the PAPI project
# REMARK(S):
#   -
#

# from the COMPILE directory

clear
mpicc -DProActiveMPI_java -I/usr/local/jdk1.4.0/include -I/usr/local/jdk1.4.0/include/linux ../src/mpi/MPI_C_Interface.c ../src/mpi/example/MPI_C_Interface_sample.c -o ../bin/mpi/libMPI_C_Interface_sample.so -shared
mpicc ../src/mpi/MPI_C_Interface.c ../src/mpi/example/MPI_C_Interface_sample.c  -o ../bin/mpi/MPI_C_Interface_sample

#
# END OF FILE
#
