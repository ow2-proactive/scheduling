@echo off
echo. 
echo --- IntegralPi --------------------------------------------------

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
set np=%1

IF "%1"=="" set np=4

echo The number of workers is %np%
echo Feel free to edit this script if you want to specify another deployement descriptor.

call init.bat
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..
%JAVA_CMD% org.objectweb.proactive.examples.integralpi.Launcher "%PROACTIVE%\descriptors\Matrix.xml" %np%
ENDLOCAL

echo. 
echo ------------------------------------------------------------------
