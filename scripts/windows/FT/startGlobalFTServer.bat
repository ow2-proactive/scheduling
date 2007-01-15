@echo off

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

SETLOCAL
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..

call "%PROACTIVE%\scripts\windows\init.bat"

echo on

%JAVA_CMD% -Xms64m -Xmx1024m org.objectweb.proactive.core.body.ft.servers.StartFTServer %1 %2 %3 %4 %5 %6

ENDLOCAL
