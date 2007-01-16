@echo off
echo. 
echo --- C3D Dispatcher WS ---------------------------------------------
echo --- (this example needs Tomcat Web Server installed and running) --------

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

rem --- Verifying current directory
goto doit

:usage
echo. 
goto end


:doit
SETLOCAL
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..

call "%PROACTIVE%\scripts\windows\init.bat"

set XMLDESCRIPTOR=..\..\..\descriptors\C3D_Dispatcher_Renderer.xml 
%JAVA_CMD%  org.objectweb.proactive.examples.webservices.c3dWS.C3DDispatcher %XMLDESCRIPTOR% %1 
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
