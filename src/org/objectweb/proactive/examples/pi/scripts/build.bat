@echo off

if NOT DEFINED JAVA_HOME goto javahome
if "%JAVA_HOME%" == "" goto javahome

SETLOCAL
set PROACTIVE=\..\..\..\..\..\..\..
set CLASSPATH=%JAVA_HOME%\lib\tools.jar;.%PROACTIVE%\ant.jar;.%PROACTIVE%\ant-launcher.jar
%JAVA_HOME%\bin\java org.apache.tools.ant.Main -buildfile build.xml %1 %2 %3 %4 %5
ENDLOCAL
goto end


:javahome
echo.
echo The enviroment variable JAVA_HOME must be set to the current jdk 
echo distribution installed on your computer.
echo Use 
echo    set JAVA_HOME=<the directory where is the JDK>
goto end


:end
