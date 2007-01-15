@echo off
echo.
echo --- Hello World example ---------------------------------------------

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
goto doit

:usage
echo. 
goto end

:doit
SETLOCAL
call init.bat

IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

REM JUST the hello launcher. No parameter. batch file asks a question.
if errorlevel 1 GOTO remote

:start
set /p choice="Do you want to use a (1)local or (2)remote descriptor file ? Simplest is local : "
if not '%choice%'=='' set choice=%choice:~0,1%
if '%choice%'=='1' goto local
if '%choice%'=='2' goto remote
ECHO "%choice%" is not valid, using local
ECHO.
goto local

:local 
set XMLDESCRIPTOR=%PROACTIVE%\descriptors\helloLocal.xml
goto launch

:remote
set XMLDESCRIPTOR=%PROACTIVE%\descriptors\helloRemote.xml

:launch

%JAVA_CMD% org.objectweb.proactive.examples.hello.Hello "%XMLDESCRIPTOR%"
ENDLOCAL

echo.
echo ----------------------------------------------------------
echo on

:end
