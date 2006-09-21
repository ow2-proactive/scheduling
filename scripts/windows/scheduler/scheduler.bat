@echo off
echo. 
echo --- Hello World ---------------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL
call %PROACTIVE%\scripts\windows\init.bat

rem This program will enable you to launch the scheduler daemon

%JAVA_CMD% org.objectweb.proactive.scheduler.Scheduler
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
