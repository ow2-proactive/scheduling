#include<stdio.h>
#include<stdlib.h>
#include<unistd.h>

#define DEFAULT_WAIT_TIME 1

int main(int argc, char**argv){
	printf("Starting native task...\n");
	/* parsing args */
	int nb = DEFAULT_WAIT_TIME;
	if(argc > 1){
		printf("argv[1]= %s\n", argv[1]);
		sscanf(argv[1], "%d", &nb);
		printf("Waiting time : %d s\n", nb);
	}
	int i;
	for(i = 0; i < 10; i++){
		printf(".");
		fflush(stdout);
		sleep(nb);
	}
	printf("\nNative task terminated !\n");

	return EXIT_SUCCESS;
}
