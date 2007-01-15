@echo off
echo. 
echo --- C3D ---------------------------------------------


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
set XMLDESCRIPTOR=..\..\descriptors\C3D_Dispatcher_Renderer.xml 
%JAVA_CMD% org.objectweb.proactive.examples.c3d.C3DDispatcher %XMLDESCRIPTOR%
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
