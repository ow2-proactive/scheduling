@echo off
echo. 
echo --- The Garden ---------------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL
call init.bat
start %JAVA_CMD% org.objectweb.proactive.StartNode ///vm1 
start %JAVA_CMD% org.objectweb.proactive.StartNode ///vm2 
pause
%JAVA_CMD% org.objectweb.proactive.examples.garden.Flower
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
