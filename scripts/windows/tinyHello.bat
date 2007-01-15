@echo off
echo.
echo --- Hello World tiny example ---------------------------------------------

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

:doit
SETLOCAL
call init.bat

IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

%JAVA_CMD% org.objectweb.proactive.examples.hello.TinyHello 
ENDLOCAL

echo.
echo ----------------------------------------------------------
