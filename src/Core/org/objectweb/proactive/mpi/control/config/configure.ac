# Process this file with autoconf to produce a configure script.
AC_INIT([org.objectweb.proactive.mpi.control.config], [1.0], [stephane.mariani@sophia.inria.fr])
AC_CONFIG_SRCDIR([src/ProActiveMPI.c])
AM_INIT_AUTOMAKE([foreign])
AM_CONFIG_HEADER([config.h])
AC_LANG([C])

# Checks for programs.
AC_PROG_CC
AC_PROG_F77

ACX_MPI
CC="$MPICC"
F77="$MPIF77"
LIBS="$MPILIBS $LIBS"

AC_CONFIG_FILES([
 Makefile
 src/Makefile
])

AC_OUTPUT
echo " "
echo "************************************************************"
echo " Enter the src/Core/org/objectweb/proactive/mpi/control/config/"
echo " directory then type 'make mpicode=myCode.c' to compile your"
echo " mpi code 'myCode.c'. It will generate a binary 'myCode' in bin/"
echo " directory."
echo ""
echo " By default, 'make' will compile the example jacobi.c included"
echo " in the source directory."
echo ""
echo " For further details please read INSTALL."
echo "************************************************************"

	