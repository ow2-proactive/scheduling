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

clear
mpicc -DPAPI_java -I/usr/local/jdk1.4.0/include -I/usr/local/jdk1.4.0/include/linux ../../../src/mpi/PAPI_int.c ../../../src/nonregressiontest/mpi/PAPI_sample.c -o ../../../bin/mpi/libPAPI_sample.so -shared
mpicc ../../../src/mpi/PAPI_int.c ../../../src/nonregressiontest/mpi/PAPI_sample.c  -o ../../../bin/mpi/PAPI_sample

#
# END OF FILE
#
