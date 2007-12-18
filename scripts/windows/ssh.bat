@echo off
echo. 
echo --- SSHClient ----------------------------------------

if "%1" == "" goto usage

goto doit

:usage
echo. 
echo Connect to a remote host through ssh
echo    - 1 : 
echo. 
echo ex : ssh.bat [-p password [-l username]] localhost  commandline
echo parameters must be given in this particular order if any
echo.
goto doit


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat
%JAVA_CMD%  org.objectweb.proactive.core.ssh.SSHClient %*
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
