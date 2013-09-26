@echo off
SETLOCAL EnableDelayedExpansion

set VERBOSE=true
set VERBOSE_JETTY=false
set PORT=8080
set REST=
set RM=
set SCHED=
set JVM_OPTS=
set RM_URL=
set SCHED_URL=
set TMPV=

:ParamParseLoop
IF [%1]==[] goto :ParamParseEndLoop
    IF %1 == -q (
        set VERBOSE=false
        goto :ParamParseShift
    ) 
    IF %1 == -v (
        set VERBOSE=true
        goto :ParamParseShift
    ) 
    IF %1 == -h (
        call :print_help
        goto :eof
    ) 
    IF %1 == -A (
        IF NOT exist "%~2" (
            echo -A must point to a Web App folder or .war file
            goto :eof
        )
        set TMPV=%2
	set REST=!TMPV:"=!

        SHIFT
        goto :ParamParseShift
    ) 
    IF %1 == -R (
        IF NOT exist "%~2" (
            echo -R must point to a Web App folder or .war file
            goto :eof
        )
        set TMPV=%2
        set RM=!TMPV:"=!
        SHIFT
        goto :ParamParseShift
    ) 
    IF %1 == -S (
        IF NOT exist "%~2" (
            echo -S must point to a Web App folder or .war file
            goto :eof
        )
        set TMPV=%2
        set SCHED=!TMPV:"=!
        SHIFT
        goto :ParamParseShift
    ) 
    IF %1 == -r (
        IF [%2]==[] (
            echo -r requires an argument
            goto :eof
        )
        set RM_URL=%2
        SHIFT
        goto :ParamParseShift
    ) 
    IF %1 == -s (
        IF [%2]==[] (
            echo -s requires an argument
            goto :eof
        )
        set SCHED_URL=%2
        SHIFT
        goto :ParamParseShift
    ) 
    IF %1 == -p (
        IF [%2]==[] (
            echo -p requires an argument
            goto :eof
        )
        set PORT=%2
        SHIFT
        goto :ParamParseShift
    ) 

    set TMP_VAR=%~1
    IF %TMP_VAR:~0,2% == -D (
        set JAVA_OPTS=%TMP_VAR% %JAVA_OPTS%    
        goto :ParamParseShift
    )

    echo Unknown option %1
    goto :eof

:ParamParseShift
    SHIFT
goto :ParamParseLoop
:ParamParseEndLoop

IF ["%REST%"]==[""] (
    echo REST Server argument is mandatory
    goto :eof
)

set REST_URL=http://localhost:%PORT%/rest/rest

call :createTempDir scheduler_start_gui
set BASE_TEMP_DIR=%_result:"=%
set REST_DIR=%BASE_TEMP_DIR%\rest
set RM_DIR=%BASE_TEMP_DIR%\rm
set LOGFILE=%BASE_TEMP_DIR%\jetty.log
set POL=%BASE_TEMP_DIR%\java.security.policy

set SCHED_DIR=%BASE_TEMP_DIR%\sched

set JAVA=%JAVA_HOME%\bin\java.exe
if NOT DEFINED JAVA_HOME set JAVA=java.exe
if "%JAVA_HOME%" == "" set JAVA=java.exe

set ERRORLEVEL=0
"%JAVA%" -version 2>NUL
IF "%ERRORLEVEL%" NEQ "0" (
    echo Java could not be found in your system, configure your PATH to find java executable or define JAVA_HOME environment variable
    goto :eof
)

set LIB_DIR=%CD%\..\..\dist\lib
set CP=%LIB_DIR%\*

mkdir %REST_DIR%
IF %VERBOSE% == true (
    echo Deploying REST Server in %REST_DIR% 
)
IF /I "%REST:~-4%" == ".war" (   
    call :unzip "%REST%" "%REST_DIR%"
) ELSE (
    xcopy /E "%REST%\*" "%REST_DIR%"
)

COPY ..\..\config\proactive\ProActiveConfiguration.xml "%REST_DIR%\WEB-INF\"

IF NOT [%RM_URL%]==[] (
    call :set_property "%REST_DIR%\WEB-INF\portal.properties" rm.url %RM_URL%
)
IF NOT [%SCHED_URL%]==[] (
    call :set_property "%REST_DIR%\WEB-INF\portal.properties" scheduler.url %SCHED_URL%
)

set APPS=%REST_DIR%

IF NOT ["%RM%"]==[""] (
    mkdir "%RM_DIR%"
    IF %VERBOSE% == true (
        echo Deploying RM UI in %RM_DIR% 
    )
    IF /I "%RM:~-4%" == ".war" (
        call :unzip "%RM%" "%RM_DIR%"
    ) ELSE (
        xcopy /E "%RM%\*" "%RM_DIR%"
    )
    call :set_property "%RM_DIR%\rm.conf" rm.rest.url %REST_URL%

    set APPS=%APPS% %RM_DIR% 
)

IF NOT ["%SCHED%"]==[""] (
    mkdir "%SCHED_DIR%"
    IF %VERBOSE% == true (
        echo Deploying Scheduling UI in %RM_DIR% 
    )
    IF /I "%SCHED:~-4%" == ".war" (
        call :unzip "%SCHED%" "%SCHED_DIR%"
    ) ELSE (
        xcopy /E "%SCHED%\*" "%SCHED_DIR%"
    )
    call :set_property "%SCHED_DIR%\scheduler.conf" sched.rest.url %REST_URL%

    set APPS=%APPS% %SCHED_DIR% 
)

set CLASS=org.ow2.proactive.utils.JettyLauncher

set LOG=
IF %VERBOSE_JETTY% == false (
    IF %VERBOSE% == true (
        echo Jetty Launcher logs to %LOGFILE%
    )
    set LOG=-l "%LOGFILE%"
)

echo grant { permission java.security.AllPermission; }; > %POL%
set JVM_OPTS=%JVM_OPTS% -Djava.security.manager -Djava.security.policy=%POL%

set CLASSPATH=%CP%
"%JAVA%" %JVM_OPTS% %CLASS% -p %PORT% %LOG% %APPS%

goto :eof

:unzip
    REM Using jruby interpreter to unzip since Windows doesn't have a built-in ZIP command
    SET ZIPFILE=%1
    SET ZIPFILE=%ZIPFILE:"=%
    SET DESTDIR=%2
    SET DESTDIR=%DESTDIR:"=%

    SETLOCAL
    SET CLASSPATH=%LIB_DIR%\*;%CLASSPATH%
    SET CMD="Java::org.ow2.proactive.scheduler.common.util.ZipUtils.unzip(Java::java.util.zip.ZipFile.new('%ZIPFILE%'),Java::java.io.File.new('%DESTDIR%'))"
    "%JAVA%" org.jruby.Main -e %CMD%
    ENDLOCAL

    goto :eof

:set_property
    REM Using jruby interpreter to change a specified property in the specified file
    REM %1 - path of the properties file
    REM %2 - property name
    REM %3 - property value

    SETLOCAL enabledelayedexpansion
    SET path=%1

    REM escape quotes in path
    Set path=!path:"=!

    set CLASSPATH=%LIB_DIR%\*
    set CMD="f=Java::java.io.File.new('%path%');prop=Java::java.util.Properties.new();prop.load(Java::java.io.FileInputStream.new(f));prop.setProperty('%2','%3');prop.store(Java::java.io.FileOutputStream.new(f),'')"
    "%JAVA%a" org.jruby.Main -e %CMD%
    ENDLOCAL

    goto :eof

:createTempDir
    SETLOCAL
    set TMPFILE=%TMP%\%1\%RANDOM%_%TIME:~-2%_%TIME:~0,2%
    if exist "%TMPFILE%" GOTO :GETTEMPNAME 
    MKDIR %TMPFILE%
    ENDLOCAL & SET _result=%TMPFILE%
    goto :eof

:print_help
    echo jetty-launcher.bat ^<arguments^> [options]
    echo.
    echo Paths for Web Applications (REST Server and both UI) point to either
    echo a valid .war file, or the extraction directory of a valid .war file.
    echo.
    echo Arguments:
    echo -A PATH   REST Server
    echo. 
    echo Options:
    echo -R PATH   RM Web UI
    echo -S PATH   Scheduler Web UI
    echo -r URL    RM Server URL
    echo -s URL    Scheduler Server URL
    echo -p PORT   HTTP server port
    echo -q        quiet output
    echo -v        Output jetty logs on stdout instead of tmp file
    echo -Dxxx     JVM option
    echo -h        print this message
    echo.
    goto :eof

:eof
ENDLOCAL
