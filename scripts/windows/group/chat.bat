@echo off
echo. 
echo --- Chat with ProActive ---------------------------------

rem if "%1" == "" goto usage

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"
%JAVA_CMD%  org.objectweb.proactive.examples.chat.Chat %*
ENDLOCAL

:usage


:end
pause
echo. 
echo ---------------------------------------------------------
