@echo off
echo. 
echo --- JMX connector client ---------------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL
IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\..\.

call %PROACTIVE%\scripts\windows\init.bat


%JAVA_CMD%  org.objectweb.proactive.examples.jmx.TestClient
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
