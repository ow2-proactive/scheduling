@echo off
echo. 

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat
%JAVA_CMD% org.objectweb.proactive.examples.fibonacci.Add
ENDLOCAL

echo. 
echo ---------------------------------------------------------
