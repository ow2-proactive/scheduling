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
start %JAVA_CMD% org.objectweb.proactive.StartNode //localhost/one 
start %JAVA_CMD% org.objectweb.proactive.StartNode //localhost/two

%JAVA_CMD% org.objectweb.proactive.examples.penguin.PenguinControler //localhost/one //localhost/two
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
