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
rem ----

IF DEFINED CLASSPATHEXT (
	SET CLASSPATH=%CLASSPATHEXT%
) ELSE (
	SET CLASSPATH=.
)

rem Check if classes exists and is not empty
IF EXIST "%PA_SCHEDULER%\classes\scheduler" ( 
	SET CLASSPATH=%CLASSPATH%;%PA_SCHEDULER%\classes\common;%PA_SCHEDULER%\classes\resource-manager;%PA_SCHEDULER%\classes\scheduler
	SET JARS=%PA_SCHEDULER%\lib\
	FOR %%j IN ("%PA_SCHEDULER%\lib\*.jar") DO SET JARS=!JARS!;%%j
	rem Use jar index to avoid 'command too long'	
	SET JARS=!JARS!;%PA_SCHEDULER%\lib\ProActive\ProActive.jar 
	FOR %%j IN ("%PA_SCHEDULER%\lib\common\*.jar") DO SET JARS=!JARS!;%%j
	rem hibernate libs
	FOR %%j IN ("%PA_SCHEDULER%\lib\hibernate\annotation\*.jar") DO SET JARS=!JARS!;%%j
	FOR %%j IN ("%PA_SCHEDULER%\lib\hibernate\core\*.jar") DO SET JARS=!JARS!;%%j
) ELSE (
	SET JAR=
	rem Jars needed by the scheduler
	FOR %%j IN ("%PA_SCHEDULER%\dist\lib\*.jar") DO SET JARS=!JARS!;%%j
)

SET CLASSPATH=%CLASSPATH%;%JARS%

rem log4j file
IF "%1"=="client" (
	set LOG4J_FILE=file:%PA_SCHEDULER%/config/log4j/scheduler-log4j-client
) ELSE (
	set LOG4J_FILE=file:%PA_SCHEDULER%/config/log4j/scheduler-log4j-server
)

set JAVA_CMD="%JAVA_HOME%\bin\java.exe" -Dproactive.home="%PA_SCHEDULER%" -Dproactive.configuration="file:%PA_SCHEDULER%\config\proactive\ProActiveConfiguration.xml" -Dpa.scheduler.home="%PA_SCHEDULER%" -Dpa.rm.home="%PA_SCHEDULER%" -Djava.security.manager -Djava.security.policy="%PA_SCHEDULER%\config\scheduler.java.policy" -Dlog4j.configuration="%LOG4J_FILE%"

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
