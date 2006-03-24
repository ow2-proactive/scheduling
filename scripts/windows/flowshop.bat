@echo off
echo. 
echo --- FlowShop ----------------------------------------

goto doit

:usage
echo. 
echo startFS.bat taillardFile deployementFile
goto end


:doit
SETLOCAL
call init.bat

if "%2" == "" goto usage

%JAVA_CMD% org.objectweb.proactive.examples.flowshop.Main -bench %1 -desc %2

ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------