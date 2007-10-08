@echo off
echo. 
echo --- Scheduler ---------------------------------------------

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

  SET SCHEDULER_URL=%1
  SET RM=%2


SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"

rem This program will enable you to launch the scheduler daemon

%JAVA_CMD% org.objectweb.proactive.examples.scheduler.LocalSchedulerExample %SCHEDULER_URL% %RM%
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
