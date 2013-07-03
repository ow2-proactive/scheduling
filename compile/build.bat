@echo off

if "%1" == "" goto projecthelp


:build
SETLOCAL
set CLASSPATH=compile\lib\ant-launcher.jar;%CLASSPATH%
echo %CLASSPATH%
cd ..
"ant" "-Dant.home=compile" -buildfile compile/build.xml %1 %2 %3 %4 %5 %WHEN_NO_ARGS%
ENDLOCAL
goto end


:projecthelp
set WHEN_NO_ARGS="-projecthelp"
goto build



:end
set WHEN_NO_ARGS=
