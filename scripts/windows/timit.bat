@echo off
echo. 
echo --- TimIt --------------------------------------------------

rem --- Verifying current directory
SET COMMAND=%0
IF NOT "%COMMAND:~-4%" == ".bat" (
 SET COMMAND=%0.bat
)
 
SET OK=0
FOR /F %%i in ('dir /b') do IF "%%i" == "%COMMAND%" SET OK=1

IF %OK% == 0 (
echo scripts must be started in the same directory as the script.
goto end
)

SETLOCAL
call init.bat
set TIMIT_DEFAULT_CONFIG_FILE=%PROACTIVE%\src\org\objectweb\proactive\examples\timit
%JAVA_CMD% org.objectweb.proactive.benchmarks.timit.TimIt -c %TIMIT_DEFAULT_CONFIG_FILE%\config.xml
ENDLOCAL

echo. 
echo ------------------------------------------------------------
