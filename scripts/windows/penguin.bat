@echo off
echo. 
echo --- Penguin ----------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL
call init.bat
start %JAVA_CMD% org.objectweb.proactive.rmi.StartNode //localhost/one 
start %JAVA_CMD% org.objectweb.proactive.rmi.StartNode //localhost/two
pause

%JAVA_CMD% org.objectweb.proactive.examples.penguin.Penguin //localhost/one //localhost/two
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
