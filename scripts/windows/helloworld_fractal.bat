@echo off
echo. 
echo --- Fractal HelloWorld example ----------------------------------------
if "%1" == "help" goto usage

goto doit

:usage
echo. 
echo helloworld-fractal.sh <parameters>
echo		
echo		parameters are :
echo			- parser
echo			- wrapper
echo			- distributed (needs parser)  echo. 
goto doit


:doit
SETLOCAL
call init.bat
%JAVA_CMD%  org.objectweb.proactive.examples.components.helloworld.HelloWorld %1 %2 %3 
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
