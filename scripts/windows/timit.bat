@echo off
echo. 
echo --- TimIt --------------------------------------------------

SETLOCAL
call init.bat
set TIMIT_DEFAULT_CONFIG_FILE=%PROACTIVE%\src\org\objectweb\proactive\examples\timit
%JAVA_CMD% org.objectweb.proactive.benchmarks.timit.TimIt -c %TIMIT_DEFAULT_CONFIG_FILE%\config.xml
ENDLOCAL

echo. 
echo ------------------------------------------------------------
