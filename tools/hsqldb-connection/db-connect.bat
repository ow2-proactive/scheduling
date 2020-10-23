@rem ##########################################################################
@rem
@rem  A Simple Shell Command To access HSQLDB CLI and connect by default to scheduler DB
@rem  If you would like to connect to another ProActive DB, please configure the sqltool.rc file before running the script
@rem
@rem ##########################################################################

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem================================================
IF exist "%APP_HOME%/jre" (set JAVA_HOME="%APP_HOME%/jre")
@rem================================================

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

:execute
@rem Execute DB connection script
"%JAVA_EXE%" -jar sqltool-2.5.1.jar --rcFile=sqltool.rc scheduler
