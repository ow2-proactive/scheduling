@echo off
echo. 
echo --- Resource Manager -------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"

%JAVA_CMD% -Xms128m -Xmx2048m org.objectweb.proactive.extensions.resourcemanager.test.util.RMLauncher %*

:end
echo. 
echo ---------------------------------------------------------

