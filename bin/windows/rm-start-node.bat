@echo off
echo.
echo --- StartNode----------------------------------------

if "%1" == "" goto usage

goto doit

:usage
echo.
echo Start a new Node
echo    - 1 : the name of the node to create
echo.
echo ex : rm-start-node.bat node1
echo.
echo Node started with a random name
echo.
goto doit


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat log4j-client
%JAVA_CMD%  org.objectweb.proactive.core.node.StartNode %*
ENDLOCAL

:end
echo.
echo ---------------------------------------------------------
