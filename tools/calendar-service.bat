@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  proactive-server startup script for Windows
@rem
@rem ##########################################################################

echo Calendar Service is starting up ...

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

@rem Add default JVM options here. You can also use JAVA_OPTS and PROACTIVE_SERVER_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-server" "-Dfile.encoding=UTF-8"

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem================================================
IF exist "%APP_HOME%/jre" (set JAVA_HOME="%APP_HOME%/jre")
@rem================================================

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/javaw.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

@rem Execute proactive-server
:execute
@rem import proactive user accounts to radicale
TYPE %APP_HOME%\config\authentication\login.cfg > %RADICLAE_HOME%\Data\config\htpasswd.txt

start "" "%JAVA_EXE%" -Dpa.scheduler.home=%APP_HOME% -Dspring.config.location=%APP_HOME%\config\calendar-service\application.properties -jar %APP_HOME%\tools\calendar-service*.jar >NUL 2>&1

echo Done

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
@rem Set variable ${exitEnvironmentVar} if you need the _script_ return code instead of
@rem the _cmd.exe /c_ return code!
if  not "" == "%${exitEnvironmentVar}%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

