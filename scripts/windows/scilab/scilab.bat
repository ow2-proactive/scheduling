REM Script used to start the scilab tools
@echo off
echo. 
echo --- Scilab example ---------------------------------------------

:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\..

call "%PROACTIVE%\scripts\windows\init.bat"
call scilab_env.bat
%JAVA_CMD% org.objectweb.proactive.extensions.scilab.gui.SciFrame

ENDLOCAL

:end
pause
echo. 
echo ---------------------------------------------------------
