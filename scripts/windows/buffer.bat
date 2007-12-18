@echo off
echo. 
echo --- Bounder Buffer ---------------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat
%JAVA_CMD% org.objectweb.proactive.examples.boundedbuffer.AppletBuffer
ENDLOCAL

:end
pause
echo. 
echo -----------------------------------------------------------------
