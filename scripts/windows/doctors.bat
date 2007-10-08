@echo off
echo. 
echo --- The Salishan problems : Problem 3 - The Doctor's Office -----

goto doit

:usage
echo. 
echo. 
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat
%JAVA_CMD% org.objectweb.proactive.examples.doctor.Office
ENDLOCAL

:end
pause
echo. 
echo -----------------------------------------------------------------
