@echo off
echo. 
echo --- Hello World Web Service ---------------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL
IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\..\.

call %PROACTIVE%\scripts\windows\init.bat

%JAVA_CMD% org.objectweb.proactive.examples.webservices.helloWorld.WSClient %1
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------