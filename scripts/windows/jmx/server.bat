@echo off
echo. 
echo --- jmx connector server ---------------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..

call "%PROACTIVE%\scripts\windows\init.bat"

%JAVA_CMD%  org.objectweb.proactive.examples.jmx.TestServer 
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
