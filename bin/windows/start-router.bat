@echo off
echo.
echo --- StartRouter----------------------------------------

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat defaultNode-log4j

%JAVA_CMD% org.objectweb.proactive.extensions.pamr.router.Main %*

ENDLOCAL

:end
echo.
echo ---------------------------------------------------------
