#
# PROJECT NAME:
#   PAPI (ProActive Parallel Interface)
# FILE NAME:
#   javarun
# DEPENDENCY FILE(S):
#   env.sh
#
# DATE:
#   10-23-03
# UPDATE:
#   10-23-03
# REVISION NUMBER:
PAPI_JAVACOMP_REV=1AA-031023-01
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
#   Run the PAPI sample
# REMARK(S):
#   -

# From the SCRIPT/UNIX directory

clear
source ../env.sh
java -Djava.library.path=../../../bin/mpi -Djava.security.manager -Djava.security.policy=../proactive.java.policy -Dlog4j.configuration=../proactive-log4j ../../../bin/mpi/MPI_C_Interface_sample


#
# END OF FILE
#

