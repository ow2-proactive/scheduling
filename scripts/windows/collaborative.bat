@echo off
echo. 
echo --- Collaborative ----------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL
call init.bat

start %JAVA_CMD% org.objectweb.proactive.rmi.StartNode //localhost/home 
start %JAVA_CMD% org.objectweb.proactive.rmi.StartNode //localhost/one 
start %JAVA_CMD% org.objectweb.proactive.rmi.StartNode //localhost/two 
start %JAVA_CMD% org.objectweb.proactive.rmi.StartNode //localhost/three 
pause

%JAVA_CMD% org.objectweb.proactive.examples.collaborative.Agent 3 //localhost/one //localhost/two //localhost/three
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
