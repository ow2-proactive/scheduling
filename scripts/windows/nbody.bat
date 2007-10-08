@echo off
echo. 
echo --- N-body with ProActive ---------------------------------

:doit
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..
SETLOCAL ENABLEDELAYEDEXPANSION

call init.bat
if "%1" equ "displayft" goto ft
if "%1" neq "-displayft" goto noft

:ft
set XMLDESCRIPTOR="%PROACTIVE%\descriptors\FaultTolerantWorkersLocal.xml"
goto cmd

:noft
set XMLDESCRIPTOR="%PROACTIVE%\descriptors\Workers.xml"
goto cmd

:cmd

%JAVA_CMD% org.objectweb.proactive.examples.nbody.common.Start %XMLDESCRIPTOR% %*
ENDLOCAL

pause
echo. 
echo ---------------------------------------------------------
