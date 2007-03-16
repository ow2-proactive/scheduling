	program main 
	include 'mpif.h'

	integer size, rank, ierr, job, n

	Call MPI_INIT( ierr )
	Call MPI_COMM_SIZE( MPI_COMM_WORLD, size, ierr )
	Call MPI_COMM_RANK( MPI_COMM_WORLD, rank, ierr )
	Call PROACTIVEMPI_INIT( rank, ierr )
	Call PROACTIVEMPI_JOB( job )
	
	print *, 'i am ' , rank, 'of', size, 'job', job,  'error code ', ierr
	if ( rank.eq.0 ) then
		n = 99
		call PROACTIVEMPI_SEND(n, 1, MPI_INTEGER, 1, 0, 0, ierr)
	else 
		call PROACTIVEMPI_RECV(n, 1, MPI_INTEGER, 0, 0, 0, ierr)
		print *, 'i am ' , rank, 'recv', n, 'from 0 error code ', ierr
	endif
	
	Call PROACTIVEMPI_FINALIZE( ierr )
	Call MPI_FINALIZE( ierr )
	end