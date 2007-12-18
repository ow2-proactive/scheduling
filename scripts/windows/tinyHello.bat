@echo off
echo.
echo --- Hello World tiny example ---------------------------------------------

:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat

IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

%JAVA_CMD% org.objectweb.proactive.examples.hello.TinyHello 
ENDLOCAL

pause
echo.
echo ----------------------------------------------------------
