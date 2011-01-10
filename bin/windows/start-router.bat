@echo off
echo.
echo --- StartRouter----------------------------------------

set ERRORLEVEL=

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat log4j-client
%JAVA_CMD% org.objectweb.proactive.extensions.pamr.router.Main %*
ENDLOCAL

:end
echo.
echo ---------------------------------------------------------

exit /B %ERRORLEVEL%
