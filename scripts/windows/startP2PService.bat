#!/bin/sh

SETLOCAL
call init.bat

	echo. 
	echo --- StartP2PService -------------------------------------
   	%JAVA_CMD% org.objectweb.proactive.p2p.core.service.StartService %1 %2 %3
   	

    echo. 
	echo ---------------------------------------------------------
