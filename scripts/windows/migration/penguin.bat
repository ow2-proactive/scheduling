@echo off
echo. 
echo --- Penguin ----------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL
IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\..\.

call %PROACTIVE%\scripts\windows\init.bat

set XMLDESCRIPTOR=..\..\..\descriptors\Penguin.xml

%JAVA_CMD% org.objectweb.proactive.examples.penguin.PenguinControler %XMLDESCRIPTOR%
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------