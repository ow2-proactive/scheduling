@echo off

IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\..\.
SETLOCAL
call %PROACTIVE%\scripts\windows\init.bat


%JAVA_CMD% -Xms64m -Xmx1024m org.objectweb.proactive.core.body.ft.util.StartFTServer %1 %2 %3 %4 %5 %6

ENDLOCAL
