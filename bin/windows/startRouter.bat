@echo off
echo.
echo --- StartRouter----------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat defaultNode-log4j

%JAVA_CMD% org.objectweb.proactive.extra.messagerouting.router.Main %*

ENDLOCAL

:end
echo.
echo ---------------------------------------------------------
