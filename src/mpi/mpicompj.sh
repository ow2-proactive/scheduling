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

# From the COMPILE directory

clear
javac ../src/mpi/ProActiveMPI.java
javah -jni ../src/mpi/ProActiveMPI
javap -s -p ../src/ProActiveMPI > ../src/mpi/ProActiveMPI.sgn
mv ../src/mpi/ProActiveMPI.class ../bin/mpi
# mv ../src/mpi/ProActiveMPI.h ../src/mpi
# mv ../src/mpi/ProAciveMPI.sgn ../src/mpi
javac ../src/mpi/example/MPI_C_Interface_sample.java
mv ../src/mpi/example/MPI_C_Interface_sample.class ../bin/mpi


#
# END OF FILE
#
