@echo off
echo. 
echo --- AgentClient----------------------------------------
if "%1" == "" goto usage

goto doit

:usage
echo. 
echo Start the Agent Client
echo    - 1 : the url of the node the active object Agent is going to be migrated
echo. 
echo ex : agentClient //localhost/Node1
echo This node has to be previously launched with the command startNode.bat //localhost/Node1
echo. 
goto end


:doit
SETLOCAL
call init.bat
%JAVA_CMD%  org.objectweb.proactive.examples.migration.AgentClient %1 %2 %3 %4 %5 %6
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
