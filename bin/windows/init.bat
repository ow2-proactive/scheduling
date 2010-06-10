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
	SET JARS=
	rem ProActive.jar : Use jar index to avoid 'command too long'
	SET JARS=!JARS!;%PA_SCHEDULER%\lib\ProActive\ProActive.jar
	rem Scheduler libraries
	FOR %%j IN ("%PA_SCHEDULER%\lib\common\*.jar") DO SET JARS=!JARS!;%%j
	FOR %%j IN ("%PA_SCHEDULER%\lib\common\script\*.jar") DO SET JARS=!JARS!;%%j
	rem hibernate libs
	FOR %%j IN ("%PA_SCHEDULER%\lib\hibernate\annotation\*.jar") DO SET JARS=!JARS!;%%j
	FOR %%j IN ("%PA_SCHEDULER%\lib\hibernate\core\*.jar") DO SET JARS=!JARS!;%%j
) ELSE (
	rem Script engines must be added to classpath to be found
	rem it must also placed before jars containing jar-index
	SET JARS=%PA_SCHEDULER%\dist\lib\script-js.jar
	SET JARS=!JARS!;%PA_SCHEDULER%\dist\lib\jruby-engine.jar
	SET JARS=!JARS!;%PA_SCHEDULER%\dist\lib\jython-engine.jar
	rem  Needed explicitly by VFS (file transfer in pre/post script
    	SET JARS=!JARS!;%PA_SCHEDULER%\dist\lib\commons-logging-1.0.4.jar
	rem fill with ProActive.jar : use jar index for proActive dependencies
	SET JARS=!JARS!;%PA_SCHEDULER%\dist\lib\ProActive.jar
	rem fill with Scheduler jars : use jar index for Scheduler dependencies
	SET JARS=!JARS!;%PA_SCHEDULER%\dist\lib\ProActive_SRM-common.jar
	SET JARS=!JARS!;%PA_SCHEDULER%\dist\lib\ProActive_ResourceManager.jar
	SET JARS=!JARS!;%PA_SCHEDULER%\dist\lib\ProActive_Scheduler-core.jar
)

SET CLASSPATH=%CLASSPATH%;%JARS%

rem log4j file
IF "%1"=="" (
	set LOG4J_FILE=file:%PA_SCHEDULER%/config/log4j/log4j-client
) ELSE (
	set LOG4J_FILE=file:%PA_SCHEDULER%/config/log4j/%1
)

rem if log4j file is client, use standard ProActive java.security.policy file
IF "%1"=="log4j-client" (
	set JAVA_POLICY=file:%PA_SCHEDULER%/config/security.java.policy-client
) ELSE (
	set JAVA_POLICY=file:%PA_SCHEDULER%/config/security.java.policy-server
)

set JAVA_CMD="%JAVA_HOME%\bin\java.exe" -Dproactive.home="%PA_SCHEDULER%" -Dproactive.configuration="file:%PA_SCHEDULER%\config\proactive\ProActiveConfiguration.xml" -Dpa.scheduler.home="%PA_SCHEDULER%" -Dpa.rm.home="%PA_SCHEDULER%" -Djava.security.manager -Djava.security.policy="%JAVA_POLICY%" -Dlog4j.configuration="%LOG4J_FILE%"

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
