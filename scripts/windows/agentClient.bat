@echo off
echo. 
echo --- Agent ----------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL
call init.bat

set XMLDESCRIPTOR=..\..\descriptors\MigratableAgent.xml

%JAVA_CMD% org.objectweb.proactive.examples.migration.AgentClient %XMLDESCRIPTOR%
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------