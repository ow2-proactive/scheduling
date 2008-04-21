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
SHIFT
:Loop
IF [%1] NEQ [] (
    SET params=%params% %1
	SHIFT
	GOTO Loop
)
goto cmd

:ccs
set XMLDESCRIPTOR="%PROACTIVE%\descriptors\WorkersApplicationCCS.xml"
SHIFT
:Loop
IF [%1] NEQ [] (
    SET params=%params% %1
	SHIFT
	GOTO Loop
)
goto cmd

:noft
set XMLDESCRIPTOR="%PROACTIVE%\descriptors\WorkersApplication.xml"
SET params=%*
goto cmd

:cmd

%JAVA_CMD% org.objectweb.proactive.examples.nbody.common.Start %XMLDESCRIPTOR% %params%
ENDLOCAL

pause
echo. 
echo ---------------------------------------------------------
