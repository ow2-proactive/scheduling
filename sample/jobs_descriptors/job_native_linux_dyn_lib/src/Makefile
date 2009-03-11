LIB=my_native_lib
NATIVE_TASK=native_exec

all: $(LIB) $(NATIVE_TASK)

$(LIB) : my_lib.c
	gcc -fPIC -shared -o ../lib$(LIB).so my_lib.c

$(NATIVE_TASK) : $(LIB)
	gcc -fPIC -o ../$(NATIVE_TASK) -L../ -l$(LIB) native_lib_task.c

clean:
	rm ../lib$(LIB).so  ../$(NATIVE_TASK)
