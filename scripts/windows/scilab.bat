REM Script used to start the scilab tools
@echo off
echo. 
echo --- Scilab example ---------------------------------------------

:doit
SETLOCAL
call init.bat
call scilab_env.bat
%JAVA_CMD% org.objectweb.proactive.ext.scilab.%1

ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
