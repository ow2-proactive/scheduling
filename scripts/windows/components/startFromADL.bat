@echo off
echo. 
echo --- Fractal C3D example ----------------------------------------
echo --- 

:doit
SETLOCAL
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..
call "%PROACTIVE%\scripts\windows\init.bat"
set JAVA_CMD=%JAVA_CMD% -Dfractal.provider=org.objectweb.proactive.core.component.Fractive
%JAVA_CMD% org.objectweb.proactive.examples.components.StartFromADL %1 %2
ENDLOCAL

:end
pause
echo. 
echo ---------------------------------------------------------
