@echo off
echo. 
echo --- ClientServer : Client---------------------------------------------

if "%1" == "" goto usage

goto doit

:usage
echo. 
echo Launch one client
echo    - 1 : the name of the client of the localhost
echo    - 2 : (optional) the hostname of the server (localhost by default)
echo. 
echo ex : cs_client client1
echo      cs_client client1 sabini
echo. 
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat
%JAVA_CMD% org.objectweb.proactive.examples.cs.Client %*
ENDLOCAL

:end
echo. 
echo -----------------------------------------------------------------
