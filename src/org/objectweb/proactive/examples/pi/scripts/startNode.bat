@echo off
echo. 
echo --- StartNode----------------------------------------
if "%1" == "" goto usage

goto doit

:usage
echo. 
echo Start a new Node
echo    - 1 : the url of the node to create
echo. 
echo ex : startNode  rmi://localhost/node1
echo ex : startNode jini://localhost/node2
echo.
echo Node started with a random name
echo. 
goto doit


:doit
SETLOCAL
cd ..\ProActive\scripts\windows
call init.bat
%JAVA_CMD%  -Dproactive.runtime.name=PA_JVM1 org.objectweb.proactive.StartNode %1 %2 %3 %4 %5 %6
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
