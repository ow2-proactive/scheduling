@echo off
echo. 
echo --- IntegralPi --------------------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
set np=%1

IF "%1"=="" set np=4

echo The number of workers is %np%
echo Feel free to edit this script if you want to specify another deployement descriptor.

call init.bat
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..
%JAVA_CMD% org.objectweb.proactive.examples.integralpi.Launcher "%PROACTIVE%\descriptors\MatrixApplication.xml" %np%
ENDLOCAL

pause
echo. 
echo ------------------------------------------------------------------
