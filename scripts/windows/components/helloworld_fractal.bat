@echo off
echo. 
echo --- Fractal HelloWorld example ----------------------------------------
echo --- 
echo --- The expected result is an exception
echo --- 

rem if "%1" == "help" goto usage

goto doit

:usage
echo. 
echo helloworld-fractal.bat <optional parameters>
echo		
echo		parameters are :
echo			- parser
echo			- wrapper
echo			- distributed (needs parser)  echo. 
goto doit


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"
set JAVA_CMD=%JAVA_CMD% -Dfractal.provider=org.objectweb.proactive.core.component.Fractive
%JAVA_CMD%  org.objectweb.proactive.examples.components.helloworld.HelloWorld %* 
ENDLOCAL

:end
pause
echo. 
echo ---------------------------------------------------------
