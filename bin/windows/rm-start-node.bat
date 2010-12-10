@echo off
echo.
echo --- StartNode----------------------------------------

if "%1" == "" goto usage

goto doit

:usage
echo.
echo Start a new Node
echo    -n : the name of the node to create
echo.
echo ex : rm-start-node.bat -n node1
echo.
echo Node started with default name
echo.
goto doit


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat log4j-client
%JAVA_CMD%  org.ow2.proactive.resourcemanager.utils.RMNodeStarter %*
ENDLOCAL

:end
echo.
echo ---------------------------------------------------------
