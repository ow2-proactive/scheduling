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

set XMLDESCRIPTOR=..\..\..\descriptors\Matrix.xml
%JAVA_CMD% org.objectweb.proactive.examples.matrix.Main 300 %XMLDESCRIPTOR%
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
