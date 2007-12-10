@echo off
echo
echo --- Communicator ----------------------------------------------

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

goto doit

:usage
echo. 
goto end


:doit

IF  "%1" == "" (
  SET SCHEDULER_URL=//localhost/SCHEDULER_NODE 
 ) ELSE (
  SET SCHEDULER_URL=%1
)

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"

%JAVA_CMD% -Dproactive.rmi.port=1234 org.objectweb.proactive.extensions.scheduler.AdminCommunicator %SCHEDULER_URL%

:end
echo. 
echo ---------------------------------------------------------