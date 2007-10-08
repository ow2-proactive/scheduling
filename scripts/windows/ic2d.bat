@echo off
echo. 
echo --- ic2d ---------------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat
%JAVA_CMD%  org.objectweb.proactive.ic2d.IC2D
ENDLOCAL

:end
pause
echo. 
echo -----------------------------------------------------------------
