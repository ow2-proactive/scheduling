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

clear
source ../env.sh
java -Djava.library.path=../../../bin/mpi -Djava.security.manager -Djava.security.policy=/net/home/rcoudarc/DEV/PAPI/BIN/proactive.java.policy -Dlog4j.configuration=/net/home/rcoudarc/DEV/PAPI/BIN/proactive-log4j ../../../bin/mpi/PAPI_sample


#
# END OF FILE
#

