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
echo.
echo Node started with a random name
echo.
goto doit


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat cli
%JAVA_CMD%  org.objectweb.proactive.core.node.StartNode %*
ENDLOCAL

:end
echo.
echo ---------------------------------------------------------
