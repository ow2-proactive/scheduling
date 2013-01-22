// TestSleep.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <windows.h>


int _tmain(int argc, _TCHAR* argv[])
{
	printf("Waiting for 600 seconds...\n");
	Sleep(600000);
	return 0;
}

