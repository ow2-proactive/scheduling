@echo off
echo.
echo --- Hello World example ---------------------------------------------

goto doit

:usage
echo. 
goto end

:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat

IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

REM JUST the hello launcher. No parameter. batch file asks a question.
if errorlevel 1 GOTO remote


:start
set XMLDESCRIPTOR=%PROACTIVE%\descriptors\helloApplication.xml
set /p choice="Do you want to use a (1)local or (2)remote descriptor file ? Simplest is local : "
if not '%choice%'=='' set choice=%choice:~0,1%
if '%choice%'=='1' goto local
if '%choice%'=='2' goto remote
ECHO "%choice%" is not valid, using local
ECHO.
goto local


:local 
set GCMD=helloDeploymentLocal.xml
goto launch

:remote
set GCMD=helloDeploymentRemote.xml

:launch

%JAVA_CMD% -Dgcmdfile=%GCMD% org.objectweb.proactive.examples.hello.Hello "%XMLDESCRIPTOR%"
ENDLOCAL

pause
echo.
echo ----------------------------------------------------------
echo on

:end
