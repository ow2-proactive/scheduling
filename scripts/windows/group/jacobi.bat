@echo off
echo. 
echo --- Jacobi : nodes initialization ---------------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\..\.

call "%PROACTIVE%\scripts\windows\init.bat"

set XMLDESCRIPTOR=..\..\..\descriptors\Matrix.xml
%JAVA_CMD% org.objectweb.proactive.examples.jacobi.Jacobi %XMLDESCRIPTOR%
ENDLOCAL

:end
pause
echo. 
echo ---------------------------------------------------------
