@echo off
echo. 
echo --- Philosophers ----------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat
%JAVA_CMD% org.objectweb.proactive.examples.philosophers.AppletPhil
ENDLOCAL

:end
pause
echo. 
echo ---------------------------------------------------------
