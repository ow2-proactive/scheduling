@echo off
echo. 
echo --- C3D Dispatcher WS ---------------------------------------------
echo --- (this example needs Tomcat Web Server installed and running) --------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..

call "%PROACTIVE%\scripts\windows\init.bat"

set XMLDESCRIPTOR=..\..\..\descriptors\C3D_Dispatcher_Renderer.xml 
%JAVA_CMD%  org.objectweb.proactive.examples.webservices.c3dWS.C3DDispatcher %XMLDESCRIPTOR% %* 
ENDLOCAL

:end
pause
echo. 
echo ---------------------------------------------------------
