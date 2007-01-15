@echo off
echo. 
echo --- Eratosthenes ----------------------------------------
echo. You may pass an XML Deployment Descriptor file as first parameter
echo. An example can be found in ProActive/descriptors/Eratosthenes.xml

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

SETLOCAL
call init.bat
%JAVA_CMD% org.objectweb.proactive.examples.eratosthenes.Main %1
ENDLOCAL

echo. 
echo ---------------------------------------------------------
