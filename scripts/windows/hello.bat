@echo off
echo. 
echo --- Hello World ---------------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL
call init.bat
%JAVA_CMD% org.objectweb.proactive.examples.hello.HelloApplet
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
