@echo off
echo. 
echo --- ClientServer : Server ---------------------------------------------

goto doit

:usage
echo. 
echo. 
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat
%JAVA_CMD% org.objectweb.proactive.examples.cs.Server
ENDLOCAL

:end
echo. 
echo -----------------------------------------------------------------
