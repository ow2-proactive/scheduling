@echo off
setLocal EnableDelayedExpansion 

if "%JAVA_HOME%"=="" goto no_jdk

set BASEDIR="%~dp0.."

for %%i in (%BASEDIR%\dist\*.jar) do (
	set CLASSPATH=%%i
)

for /r %BASEDIR%\lib %%a in (*.jar) do (
	set CLASSPATH=!CLASSPATH!;%%a
)

set CMD_LINE_ARGS=
:set_args
if ""%1""=="""" goto done_set_args
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto set_args
:done_set_args

"%JAVA_HOME%"\bin\java -classpath "%CLASSPATH%" org.ow2.proactive_grid_cloud_portal.cli.SchedulerEntryPoint %CMD_LINE_ARGS%

goto end

:no_jdk
echo "The environment variable JAVA_HOME must be set to the current jdk distribution"
goto end

:end