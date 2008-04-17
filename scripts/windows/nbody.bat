@echo off
echo. 
echo --- N-body with ProActive ---------------------------------

:doit
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..
SETLOCAL ENABLEDELAYEDEXPANSION

call init.bat
if "%1" equ "displayft" goto ft
if "%1" equ "-displayft" goto ft
if "%1" equ "-ccs" goto ccs
if "%1" equ "ccs" goto ccs
goto noft

:ft
set XMLDESCRIPTOR="%PROACTIVE%\descriptors\FaultTolerantWorkersLocal.xml"
goto cmd

:ccs
set XMLDESCRIPTOR="%PROACTIVE%\descriptors\WorkersApplicationCCS.xml"
goto cmd

:noft
set XMLDESCRIPTOR="%PROACTIVE%\descriptors\WorkersApplication.xml"
goto cmd

:cmd

%JAVA_CMD% org.objectweb.proactive.examples.nbody.common.Start %XMLDESCRIPTOR% %*
ENDLOCAL

pause
echo. 
echo ---------------------------------------------------------
