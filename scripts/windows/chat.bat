@echo off
echo. 
echo --- Chat with ProActive ---------------------------------
if "%1" == "" goto usage

SETLOCAL
call init.bat
%JAVA_CMD%  org.objectweb.proactive.examples.chat.Chat %1 %2 %3
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
