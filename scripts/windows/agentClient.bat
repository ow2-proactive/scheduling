@echo off
echo. 
echo --- UpperClient----------------------------------------
if "%1" == "" goto usage

goto doit

:usage
echo. 
echo Start the upper Client
echo    - 1 : the url of the node the active object Upper is going to be migrated
echo. 
echo ex : upperClient //localhost/Node1
echo This node has to be previously launched with the command startNode.bat //localhost/Node1
echo. 
goto end


:doit
SETLOCAL
call init.bat
%JAVA_CMD%  org.objectweb.proactive.examples.upper.UpperClient %1 %2 %3 %4 %5 %6
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
