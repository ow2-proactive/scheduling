@echo off
echo. 
echo --- Hello World Web Service ---------------------------------------------
echo --- (this example needs Tomcat Web Server installed and running) --------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..

call "%PROACTIVE%\scripts\windows\init.bat"

%JAVA_CMD% org.objectweb.proactive.examples.webservices.helloWorld.WSClient %*
ENDLOCAL

:end
pause
echo. 
echo ---------------------------------------------------------