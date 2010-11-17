@echo off
echo.

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat log4j-client
%JAVA_CMD% org.objectweb.proactive.extensions.vfsprovider.console.PADataserverStarter %*

ENDLOCAL

:end
echo.
