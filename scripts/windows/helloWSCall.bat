@echo off
echo. 
echo --- Hello World Web Service ---------------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL
call init.bat


%JAVA_CMD% org.objectweb.proactive.examples.webservices.helloWorld.WSClient
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------