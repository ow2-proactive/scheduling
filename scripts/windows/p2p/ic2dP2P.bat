@echo off
echo. 
echo --- ic2d ---------------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION enabledelayedexpansion
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..

call "%PROACTIVE%\scripts\windows\init.bat"

%JAVA_CMD%  -Dproactive.ic2d.hidep2pnode=true org.objectweb.proactive.ic2d.IC2D
ENDLOCAL

:end
echo. 
echo -----------------------------------------------------------------
