@echo off
echo. 
echo --- Fractal HelloWorld example ----------------------------------------
echo --- 
echo --- The expected result is an exception
echo --- 

rem --- Verifying current directory
SET COMMAND=%0
IF NOT "%COMMAND:~-4%" == ".bat" (
 SET COMMAND=%0.bat
)
 
SET OK=0
FOR /F %%i in ('dir /b') do IF "%%i" == "%COMMAND%" SET OK=1

IF %OK% == 0 (
echo scripts must be started in the same directory as the script.
goto end
)

rem if "%1" == "help" goto usage

goto doit

:usage
echo. 
echo helloworld-fractal.sh <optional parameters>
echo		
echo		parameters are :
echo			- parser
echo			- wrapper
echo			- distributed (needs parser)  echo. 
goto doit


:doit
SETLOCAL
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"
set JAVA_CMD=%JAVA_CMD% -Dfractal.provider=org.objectweb.proactive.core.component.Fractive
%JAVA_CMD%  org.objectweb.proactive.examples.components.helloworld.HelloWorld %1 %2 %3 
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
