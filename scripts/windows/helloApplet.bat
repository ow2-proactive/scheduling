@echo off
echo. 
echo --- Hello World ---------------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat

rem For creating the hello object on a remote node simply pass the url of 
rem the node in parameter. If the node cannot be found it will be 
rem created locally.
rem Remote host url template is  : //remotehost/node1, can be added as parameter

%JAVA_CMD% org.objectweb.proactive.examples.hello.HelloApplet %*
ENDLOCAL

:end
pause
echo. 
echo ---------------------------------------------------------
