@echo off
echo. 
echo --- Results retriever -------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat

%JAVA_CMD% org.ow2.proactive.scheduler.examples.GetJobResult %*
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
