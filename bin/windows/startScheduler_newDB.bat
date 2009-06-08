@echo off
echo.

IF EXIST ..\..\SCHEDULER_DB (
    RMDIR /S /Q ..\..\SCHEDULER_DB
    DEL ..\..\.logs\derby.log
)
SETLOCAL ENABLEDELAYEDEXPANSION

call startScheduler.bat %*

ENDLOCAL
