@echo off
echo. 
echo --- C3D ---------------------------------------------

goto doit

:usage
echo. 
goto end


:doit
IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\..\.
SETLOCAL
call %PROACTIVE%\scripts\windows\init.bat

set XMLDESCRIPTOR=..\..\descriptors\C3D_User.xml
%JAVA_CMD% org.objectweb.proactive.examples.webservices.c3dWS.C3DUser %XMLDESCRIPTOR%
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
