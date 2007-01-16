@echo off
echo. 
echo --- SSHClient ----------------------------------------

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
echo Connect to a remote host through ssh
echo    - 1 : 
echo. 
echo ex : ssh.bat [-p password [-l username]] localhost  commandline
echo parameters must be given in this particular order if any
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
