
all: CommonInternalApi.o ProActiveMPI.o 

ProActiveMPI.o: ProActiveMPI.c
	$(MPICC) $(LDFLAGS) $(CPPFLAGS) -c $<

CommonInternalApi.o: CommonInternalApi.c
	$(MPICC)  $(LDFLAGS) $(CPPFLAGS) -c $<

			
.c.o:
	$(MPICC)  $(LDFLAGS) $(CPPFLAGS)  -c $<
	mv $(mpicode) ../bin

.f.o:
	$(MPIF77)  $(LDFLAGS) $(CPPFLAGS) -fno-second-underscore -c $<
	mv $(mpicode) ../bin
	
clean:
	-rm *.o 2>/dev/null
	-rm -f ../bin/*
