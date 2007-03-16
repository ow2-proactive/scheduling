@echo off

if NOT DEFINED JAVA_HOME goto javahome
if "%JAVA_HOME%" == "" goto javahome
if "%1" == "" goto projecthelp


:build
SETLOCAL
set CLASSPATH=lib\ant-launcher.jar;%CLASSPATH%
echo %CLASSPATH%
"%JAVA_HOME%\bin\java" "-Dant.home=." "-Dant.library.dir=./lib"  -Xmx256000000 org.apache.tools.ant.launch.Launcher -buildfile build.xml %1 %2 %3 %4 %5 %WHEN_NO_ARGS%
ENDLOCAL
goto end


:projecthelp
set WHEN_NO_ARGS="-projecthelp"
goto build


:javahome
echo.
echo The enviroment variable JAVA_HOME must be set to the current jdk 
echo distribution installed on your computer.
echo Use 
echo    set JAVA_HOME=<the directory where is the JDK>
goto end


:end
set WHEN_NO_ARGS=
