@echo off
echo. 
echo --- TimIt --------------------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\..

call "%PROACTIVE%\scripts\windows\init.bat"

set TIMIT_DEFAULT_CONFIG_FILE_PATH="%PROACTIVE%\scripts\windows\timit"
%JAVA_CMD% org.objectweb.proactive.benchmarks.timit.TimIt -c %TIMIT_DEFAULT_CONFIG_FILE_PATH%\config.xml
ENDLOCAL

echo. 
echo ------------------------------------------------------------
