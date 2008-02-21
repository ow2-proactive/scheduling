#include<stdio.h>
#include<stdlib.h>
#include<unistd.h>

#define DEFAULT_WAIT_TIME 1

int main(int argc, char**argv){
	printf("Starting native task with library needed...\n");
	/* parsing args */
	int nb = DEFAULT_WAIT_TIME;
	if(argc > 1){
		printf("argv[1]= %s\n", argv[1]);
		sscanf(argv[1], "%d", &nb);
		printf("Waiting time : %d s\n", nb);
	}

	print_dots(nb);

	printf("\nNative task terminated !\n");
	
	return EXIT_SUCCESS;
}
