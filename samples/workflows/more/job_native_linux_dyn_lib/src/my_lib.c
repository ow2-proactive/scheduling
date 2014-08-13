#include<stdio.h>
#include<stdlib.h>
#include<unistd.h>

int print_dots(int nb)
{
	int i;
	for(i = 0; i < 10; i++){
		printf(".");
		fflush(stdout);
		sleep(nb);
	}
};
