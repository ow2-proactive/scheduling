@echo off
echo. 
echo --- Results retriever -------------------------------------

  SET SCHEDULER_URL=%1

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"

%JAVA_CMD% org.objectweb.proactive.extensions.scheduler.examples.GetJobResult %SCHEDULER_URL%

:end
echo. 
echo ---------------------------------------------------------