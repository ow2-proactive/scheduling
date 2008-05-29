rem @echo off
echo.
echo --- StartDeamon -------------------------------------


goto doit

:usage
echo. 
goto end


:doit
SETLOCAL ENABLEDELAYEDEXPANSION enabledelayedexpansion
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..

call "%PROACTIVE%\scripts\windows\init.bat"

echo %CLASSPATH%

%JAVA_CMD% org.objectweb.proactive.extra.p2p.daemon.WinDaemon %*
pause
rem exit %errorlevel%

rem endlocal
