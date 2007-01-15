@echo off
echo. 
echo --- Hello World Web Service ---------------------------------------------
echo --- (this example needs Tomcat Web Server installed and running) --------
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
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..

call "%PROACTIVE%\scripts\windows\init.bat"

%JAVA_CMD% org.objectweb.proactive.examples.webservices.helloWorld.WSClient %1
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------