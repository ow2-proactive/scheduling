@echo off
echo. 
echo --- Matrix : nodes initialization ---------------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..

call "%PROACTIVE%\scripts\windows\init.bat"

set XMLDESCRIPTOR=..\..\..\descriptors\Matrix.xml
%JAVA_CMD% org.objectweb.proactive.examples.matrix.Main 300 %XMLDESCRIPTOR%
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
