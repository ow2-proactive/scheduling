@echo off
echo.
echo --- Hello World tiny example ---------------------------------------------

:doit
SETLOCAL
call init.bat

IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\.

%JAVA_CMD% org.objectweb.proactive.examples.hello.TinyHello 
ENDLOCAL

echo.
echo ----------------------------------------------------------
