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

rem For creating the hello object on a remote node simply pass the url of 
rem the node in parameter. If the node cannot be found it will be 
rem created locally.

%JAVA_CMD% org.objectweb.proactive.examples.hello.HelloApplet //remotehost/node1
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
