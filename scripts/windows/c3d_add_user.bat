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

%JAVA_CMD% org.objectweb.proactive.examples.c3d.C3DUser
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
