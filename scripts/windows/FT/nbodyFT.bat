@echo off

echo Starting Fault-Tolerant version of ProActive NBody...

IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\..\.
SETLOCAL

call %PROACTIVE%\scripts\windows\nbody.bat -displayft 4 3000

ENDLOCAL