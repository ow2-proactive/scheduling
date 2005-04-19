@echo off

echo Starting Fault-Tolerant version of ProActive NBody...

SETLOCAL
IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\..\.

call %PROACTIVE%\scripts\windows\nbody.bat -displayft 4 3000

ENDLOCAL