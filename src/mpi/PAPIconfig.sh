#
# PROJECT NAME:
#   MPICH Setup
# FILE NAME:
#   mpi.config
# DEPENDENCY FILE(S):
#   -
#
# DATE:
#   10-23-03
# UPDATE:
#   10-23-03
# REVISION NUMBER:
MPICH_SETUP_REV=100-031023-01
# CHECKED:
#   Yes
#
# LANGUAGE:
#   BASH
# COMPILATION LINE:
#   source ConfigFileName
# ENVIRONMENT CONFIGURATION:
#   -
#
# DESCRIPTION:
#   Set the Bash environment up in order to use MPICH
# REMARK(S):
#   -

echo "_ MPICH Setup (" $MPICH_SETUP_REV ") > Started on" $HOSTNAME "which is a" $HOSTTYPE "under" $OSTYPE "..."

echo "- MPICH Setup (" $MPICH_SETUP_REV ") > Environment variables initialisation"
PATH=$PATH:/usr/local/mpi:/usr/local/mpi/bin:/usr/local/mpi/include:/usr/local/mpi/lib
MANPATH=$MANPATH:/usr/local/mpi/man
export PATH
export MANPATH


# Trick to set PWD to /net/home over /0/user for rsh access
echo "- MPICH Setup (" $MPICH_SETUP_REV ") > Switching into the HOME directory"
cd $HOME


echo "_ MPICH Setup (" $MPICH_SETUP_REV ") > ... Finished on" $HOSTNAME

#
# END OF FILE
#
