@echo off
echo. 
echo --- Chat with ProActive ---------------------------------
if "%1" == "" goto usage

SETLOCAL
call init.bat
%JAVA_CMD%  org.objectweb.proactive.examples.nbody.Start %1 %2 %3 %4
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
