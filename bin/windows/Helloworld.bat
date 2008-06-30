@echo off
echo. 
echo --- Hello World---------------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat

%JAVA_CMD% org.ow2.proactive.scheduler.examples.SimpleHelloWorld
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
