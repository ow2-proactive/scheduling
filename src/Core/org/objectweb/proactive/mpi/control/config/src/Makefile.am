mpicode=jacobi

all: ProActiveMPI.o $(mpicode).o

ProActiveMPI.o: ProActiveMPI.c
	$(MPICC) -c $<
			
.c.o:
	$(MPICC) -c $<
	$(MPICC) $(mpicode).o ProActiveMPI.o -o $(mpicode)
	mv $(mpicode) ../bin

.f.o:
	$(MPIF77) -fno-second-underscore -c $<
	$(MPIF77) -fno-second-underscore $(mpicode).o ProActiveMPI.o -o $(mpicode)
	mv $(mpicode) ../bin
	
clean:
	-rm *.o 2>/dev/null
	-rm -f ../bin/*