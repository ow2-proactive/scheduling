@echo off
echo. 
echo --- Agent ----------------------------------------

goto doit

:usage
echo. 
goto end


:doit
IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\..\.
SETLOCAL
call %PROACTIVE%\scripts\windows\init.bat

set XMLDESCRIPTOR=..\..\..\descriptors\MigratableAgent.xml

%JAVA_CMD% org.objectweb.proactive.examples.migration.AgentClient %XMLDESCRIPTOR%
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------