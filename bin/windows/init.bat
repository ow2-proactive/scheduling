@echo off
rem ----------------------------------------------------------------------------
rem
rem This variable should be set to the directory where is installed ProActive
rem

IF NOT DEFINED PA_SCHEDULER set PA_SCHEDULER=%CD%\..\..

rem ----------------------------------------------------------------------------


set JAVA=%JAVA_HOME%\bin\java.exe
if NOT DEFINED JAVA_HOME set JAVA=java.exe
if "%JAVA_HOME%" == "" set JAVA=java.exe

set ERRORLEVEL=0
"%JAVA%" -version 2>NUL
IF "%ERRORLEVEL%" NEQ "0" (
    echo Java could not be found in your system, configure your PATH to find java executable or define JAVA_HOME environment variable
    goto end
)

rem ----
rem Set up the classpath using classes dir or jar files
rem ----

IF DEFINED CLASSPATHEXT (
	SET CLASSPATH=%CLASSPATHEXT%
) ELSE (
	SET CLASSPATH=.
)

rem Check if classes exists and is not empty

rem Script engines must be added to classpath to be found
rem it must also placed before jars containing jar-index
SET JARS=!JARS!;%PA_SCHEDULER%\dist\lib\jruby-1.7.4.jar
SET JARS=!JARS!;%PA_SCHEDULER%\dist\lib\sigar.jar
SET JARS=!JARS!;%PA_SCHEDULER%\dist\lib\jython-2.5.4-rc1.jar
SET JARS=!JARS!;%PA_SCHEDULER%\dist\lib\groovy-all-2.1.6.jar
rem  Needed explicitly by VFS (file transfer in pre/post script
SET JARS=!JARS!;%PA_SCHEDULER%\dist\lib\commons-logging-1.1.1.jar
SET JARS=!JARS!;%PA_SCHEDULER%\dist\lib\commons-httpclient-3.1.jar
SET JARS=!JARS!;%PA_SCHEDULER%\dist\lib\*
FOR %%j IN ("%PA_SCHEDULER%\addons\*.jar") DO SET JARS=!JARS!;%%j


SET CLASSPATH=%CLASSPATH%;%JARS%;%PA_SCHEDULER%\addons

rem log4j file
IF "%1"=="" (
	set LOG4J_FILE=file:/%PA_SCHEDULER:\=/%/config/log4j/log4j-client
) ELSE (
	set LOG4J_FILE=file:/%PA_SCHEDULER:\=/%/config/log4j/%1
)

rem if log4j file is server, use server security manager
rem otherwise, use standard ProActive java.security.policy file
IF "%1"=="scheduler-log4j-server" (
	GOTO sjps
)
IF "%1"=="rm-log4j-server" (
	:sjps
	set JAVA_POLICY=file:%PA_SCHEDULER%/config/security.java.policy-server
	GOTO sjps_
) ELSE (
	set JAVA_POLICY=file:%PA_SCHEDULER%/config/security.java.policy-client
)
:sjps_

set JAVA_CMD="%JAVA%" -Dproactive.home="%PA_SCHEDULER%" -Dproactive.configuration="%PA_SCHEDULER%\config\proactive\ProActiveConfiguration.xml" -Dpa.scheduler.home="%PA_SCHEDULER%" -Dpa.rm.home="%PA_SCHEDULER%" -Djava.security.manager -Djava.security.policy="%JAVA_POLICY%" -Dlog4j.configuration="%LOG4J_FILE%"

rem Adding java tools to the path
SET OK=1
FOR /F "delims=;" %%i IN ("%PATH%") DO (
IF /I "%%i" == "%JAVA_HOME%\bin" SET OK=0
)
IF /I %OK%==1 SET PATH=%JAVA_HOME%\bin;%PATH%

goto end



:end
