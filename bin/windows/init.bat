@echo off
rem ----------------------------------------------------------------------------
rem
rem This variable should be set to the directory where is installed ProActive
rem

IF NOT DEFINED PA_SCHEDULER set PA_SCHEDULER=%CD%\..\..

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


IF EXIST "%PA_SCHEDULER%\classes" ( 
	SET CLASSPATH=%CLASSPATH%;%PA_SCHEDULER%\classes\resource-manager;%PA_SCHEDULER%\classes\scheduler
	SET JARS=%PA_SCHEDULER%\lib\
	FOR %%j IN ("%PA_SCHEDULER%\lib\*.jar") DO SET JARS=!JARS!;%%j
rem Use jar index to avoid 'command too long'	
	SET JARS=!JARS!;%PA_SCHEDULER%\lib\ProActive\ProActive.jar 
	FOR %%j IN ("%PA_SCHEDULER%\lib\common\*.jar") DO SET JARS=!JARS!;%%j
) ELSE (
	FOR %%j IN ("%PA_SCHEDULER%\dist\lib\*.jar") DO SET JARS=!JARS!;%%j
	SET CLASSPATH=%CLASSPATH%;%PA_SCHEDULER%\dist\lib\.jar
)

SET CLASSPATH=%CLASSPATH%;%JARS%

set JAVA_CMD="%JAVA_HOME%\bin\java.exe" -Dproactive.home="%PA_SCHEDULER%" -Dpa.scheduler.home="%PA_SCHEDULER%" -Dpa.rm.home="%PA_SCHEDULER%" -Djava.security.manager -Djava.security.policy="%PA_SCHEDULER%\config\scheduler.java.policy" -Dlog4j.configuration="%PA_SCHEDULER%/config/scheduler-log4j"

rem Adding java tools to the path
SET OK=1
FOR /F "delims=;" %%i IN ("%PATH%") DO (
IF /I "%%i" == "%JAVA_HOME%\bin" SET OK=0
)
IF /I %OK%==1 SET PATH=%JAVA_HOME%\bin;%PATH%

goto end


:javahome
echo.
echo The environment variable JAVA_HOME must be set the current jdk distribution
echo installed on your computer. 
echo Use 
echo    set JAVA_HOME=<the directory where is the JDK>
goto end

 
:end
