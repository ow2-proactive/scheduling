@echo off
rem ----------------------------------------------------------------------------
rem
rem This variable should be set to the directory where is installed ProActive
rem

IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

rem ----------------------------------------------------------------------------


if NOT DEFINED JAVA_HOME goto javahome
if "%JAVA_HOME%" == "" goto javahome

rem ----
rem Set up the classpath using classes dir or jar files
rem 

IF DEFINED CLASSPATHEXT (
SET CLASSPATH=%CLASSPATHEXT%
) ELSE (
SET CLASSPATH=.
)

IF EXIST "%PROACTIVE%\classes" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\classes\Core;%PROACTIVE%\classes\Extensions;%PROACTIVE%\classes\Extra;%PROACTIVE%\classes\Examples;%PROACTIVE%\classes\IC2D-old
IF EXIST "%PROACTIVE%\ProActive.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\ProActive.jar
IF EXIST "%PROACTIVE%\ProActive_examples.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\ProActive_examples.jar

set JARS=
FOR %%j IN (%PROACTIVE%\lib\*.jar) DO SET JARS=!JARS!;%%j
SET CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib;%JARS%


set JAVA_CMD="%JAVA_HOME%\bin\java.exe" -Djava.security.manager -Djava.security.policy="%PROACTIVE%\scripts\proactive.java.policy" -Dlog4j.configuration=file:"%PROACTIVE%\scripts\proactive-log4j"

rem Adding java tools to the path
SET OK=1
FOR /F "delims=;" %%i IN ("%PATH%") DO (
IF /I "%%i" == "%JAVA_HOME%\bin" SET OK=0
)
IF /I %OK%==1 SET PATH=%JAVA_HOME%\bin;%PATH%

goto end


:javahome
echo.
echo The enviroment variable JAVA_HOME must be set the current jdk distribution
echo installed on your computer. 
echo Use 
echo    set JAVA_HOME=<the directory where is the JDK>
goto end

 
:end
