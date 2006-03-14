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
echo ex : ssh.bat [-p password] username@localhot  commandline
echo.
goto doit


:doit
SETLOCAL
call init.bat
%JAVA_CMD%  org.objectweb.proactive.core.ssh.SSHClient %1 %2 %3 %4 %5 %6
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
