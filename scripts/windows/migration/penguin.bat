@echo off
echo. 
echo --- Penguin ----------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..

call "%PROACTIVE%\scripts\windows\init.bat"

set XMLDESCRIPTOR=..\..\..\descriptors\Penguin.xml

%JAVA_CMD% org.objectweb.proactive.examples.penguin.PenguinControler %XMLDESCRIPTOR%
ENDLOCAL

:end
pause
echo. 
echo ---------------------------------------------------------