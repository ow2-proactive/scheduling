@echo off
echo.

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat
%JAVA_CMD% org.objectweb.proactive.extensions.vfsprovider.console.PAProviderServerStarter %*

ENDLOCAL

:end
echo.
