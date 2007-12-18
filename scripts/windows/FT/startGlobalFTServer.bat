@echo off

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..

call "%PROACTIVE%\scripts\windows\init.bat"

%JAVA_CMD% -Xms64m -Xmx1024m org.objectweb.proactive.core.body.ft.servers.StartFTServer %*

ENDLOCAL
pause