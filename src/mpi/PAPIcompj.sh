#
# PROJECT NAME:
#   PAPI (ProActive Parallel Interface)
# FILE NAME:
#   javacomp
# DEPENDENCY FILE(S):
#   -
#
# DATE:
#   10-23-03
# UPDATE:
#   08-04-04
# REVISION NUMBER:
PAPI_JAVACOMP_REV=1AA-040804-00
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
#   Compile the JAVA part of the PAPI project
# REMARK(S):
#   -

clear
javac ../../../src/mpi/PAPI.java
javah -jni ../../../src/mpi/PAPI
javap -s -p ../../../src/mpi/PAPI > ../../../src/mpi/PAPI.sgn
mv ../../../src/mpi/PAPI.class ../../../bin/mpi
mv ../../../src/mpi/PAPI.h ../MPI
mv ../../../src/mpi/PAPI.sgn ../MPI
javac ../../../src/nonregressiontest/mpi/PAPI_sample.java
mv ../../../src/nonregressiontest/mpi/PAPI_sample.class ../../../bin/mpi


#
# END OF FILE
#
