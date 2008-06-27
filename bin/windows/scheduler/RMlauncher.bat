@echo off
echo. 
echo --- Resource Manager -------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"

%JAVA_CMD% org.objectweb.proactive.extensions.resourcemanager.utils.RMLauncher %*

:end
echo. 
echo ---------------------------------------------------------

