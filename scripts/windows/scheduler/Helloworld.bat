@echo off
echo. 
echo --- Hello World---------------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"


%JAVA_CMD% org.objectweb.proactive.extensions.scheduler.examples.SimpleHelloWorld
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
