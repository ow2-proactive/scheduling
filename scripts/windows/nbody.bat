@echo off
echo. 
echo --- N-body with ProActive ---------------------------------
IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\.
SETLOCAL

call %PROACTIVE%\scripts\windows\init.bat

IF %1 == "displayft"
set XMLDESCRIPTOR=%PROACTIVE%\descriptors\FaultTolerantWorkers.xml
else
set XMLDESCRIPTOR=%PROACTIVE%\descriptors\Workers.xml

%JAVA_CMD% org.objectweb.proactive.examples.nbody.Start %XMLDESCRIPTOR% %1 %2 %3


ENDLOCAL

echo. 
echo ---------------------------------------------------------
