@echo off
echo. 
echo --- StartNode----------------------------------------

rem --- Verifying current directory
SET COMMAND=%0
IF NOT "%COMMAND:~-4%" == ".bat" (
 SET COMMAND=%0.bat
)
 
SET OK=0
FOR /F %%i in ('dir /b') do IF "%%i" == "%COMMAND%" SET OK=1

IF %OK% == 0 (
echo scripts must be started in the same directory as the script.
goto end
)

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
call init.bat
%JAVA_CMD%  org.objectweb.proactive.core.node.StartNode %1 %2 %3 %4 %5 %6
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
