@echo off
echo.
echo --- Hello World example ---------------------------------------------

goto doit

:usage
echo. 
goto end

:doit
SETLOCAL
call init.bat

IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\.

REM JUST the hello launcher. No parameter. batch file asks a question.

CHOICE /C:LR "Do you want to use a Local or remote descriptor file ? Simplest is local  "
if errorlevel 1 GOTO remote

:local 
set XMLDESCRIPTOR=%PROACTIVE%\descriptors\helloLocal.xml
goto launch

:remote
set XMLDESCRIPTOR=%PROACTIVE%\descriptors\helloRemote.xml

:launch

%JAVA_CMD% org.objectweb.proactive.examples.hello.Hello %XMLDESCRIPTOR%
ENDLOCAL

echo.
echo ----------------------------------------------------------
