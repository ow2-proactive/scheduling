@echo off
echo. 
echo --- ClientServer : Client---------------------------------------------

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

if "%1" == "" goto usage

goto doit

:usage
echo. 
echo Launch one client
echo    - 1 : the name of the client of the localhost
echo    - 2 : (optional) the hostname of the server (localhost by default)
echo. 
echo ex : cs_client client1
echo      cs_client client1 sabini
echo. 
goto end


:doit
SETLOCAL
call init.bat
%JAVA_CMD% org.objectweb.proactive.examples.cs.Client %*
ENDLOCAL

:end
echo. 
echo -----------------------------------------------------------------
