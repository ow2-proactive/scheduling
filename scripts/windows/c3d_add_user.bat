@echo off
echo. 
echo --- C3D ---------------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL
call init.bat
start %JAVA_CMD% org.objectweb.proactive.rmi.StartNode //localhost/users
sleep 3
%JAVA_CMD% org.objectweb.proactive.examples.c3d.C3DUser //localhost/users
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
