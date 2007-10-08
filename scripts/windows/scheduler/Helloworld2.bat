@echo off
echo. 
echo --- Hello World2---------------------------------------------

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

  SET SCHEDULER_URL=%3

SET TASKS=%1
SET SLEEP=%2
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"

rem This program will enable you to launch the scheduler daemon

%JAVA_CMD% org.objectweb.proactive.examples.scheduler.HelloWorld2 %TASKS% %SLEEP% %SCHEDULER_URL%
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
