@echo off
echo. 
echo --- N-body with ProActive ---------------------------------
rem --- Verifying current directory

SET COMMAND=%0
IF NOT "%COMMAND:~-4%" == ".bat" (
 SET COMMAND=%0.bat
)
 
SET OK=0
FOR /F %%i in ('dir /b') do IF "%%i" == "%COMMAND%" SET OK=1

IF %OK% == 0 (
echo scripts must be started in the same directory as the script.
goto end
)

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL
IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\.


call "%PROACTIVE%\scripts\windows\init.bat"
if "%1" equ "displayft" goto ft
if "%1" neq "-displayft" goto noft

:ft
set XMLDESCRIPTOR="%PROACTIVE%\descriptors\FaultTolerantWorkersLocal.xml"
goto cmd

:noft
set XMLDESCRIPTOR="%PROACTIVE%\descriptors\Workers.xml"
goto cmd

:cmd
%JAVA_CMD% org.objectweb.proactive.examples.nbody.common.Start %XMLDESCRIPTOR% %1 %2 %3
ENDLOCAL


echo. 
echo ---------------------------------------------------------
