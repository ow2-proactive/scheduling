@echo off
echo. 
echo --- Hello World ---------------------------------------------

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
rem Remote host url template is  : //remotehost/node1, can be added as parameter

%JAVA_CMD% org.objectweb.proactive.examples.hello.HelloApplet %1
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
