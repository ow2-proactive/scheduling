@echo off
echo. 
echo --- N-body with ProActive ---------------------------------
goto doit

:usage
echo. 
goto end


:doit
IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\.

SETLOCAL
call %PROACTIVE%\scripts\windows\init.bat
if "%1" == "-displayft" goto ft
if "%1" == "" goto noft

:ft
set XMLDESCRIPTOR=%PROACTIVE%\descriptors\FaultTolerantWorkers.xml

:noft
set XMLDESCRIPTOR=%PROACTIVE%\descriptors\Workers.xml

%JAVA_CMD% org.objectweb.proactive.examples.nbody.common.Start %XMLDESCRIPTOR% %1 %2 %3


ENDLOCAL

echo. 
echo ---------------------------------------------------------
