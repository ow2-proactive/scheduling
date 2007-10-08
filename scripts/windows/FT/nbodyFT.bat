@echo off

echo Starting Fault-Tolerant version of ProActive NBody...

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..

PUSHD ..

call nbody.bat -displayft 4 3000

POPD

ENDLOCAL
pause