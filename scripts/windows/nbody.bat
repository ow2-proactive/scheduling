@echo off
echo. 
echo --- N-body with ProActive ---------------------------------

SETLOCAL

call init.bat
set XMLDESCRIPTOR=..\..\descriptors\Workers.xml

%JAVA_CMD% org.objectweb.proactive.examples.nbody.Start %XMLDESCRIPTOR% %1 %2 %3


ENDLOCAL

echo. 
echo ---------------------------------------------------------
