@echo off

if NOT DEFINED JAVA_HOME goto javahome
if "%JAVA_HOME%" == "" goto javahome
if "%1" == "" goto projecthelp


:build
SETLOCAL
set CLASSPATH=%JAVA_HOME%\lib\tools.jar;ant.jar;ant-launcher.jar;xercesImpl.jar;xml-apis.jar;doclet.jar;%CLASSPATH%
echo %CLASSPATH%
"%JAVA_HOME%\bin\java" -Xmx256000000 org.apache.tools.ant.Main -buildfile proactive.xml %1 %2 %3 %4 %5 %WHEN_NO_ARGS%
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
