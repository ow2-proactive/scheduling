@echo off
SETLOCAL
SETLOCAL ENABLEDELAYEDEXPANSION

call init.bat scheduler-log4j-server

set JPS="%JAVA_HOME%\bin\jps.exe"
IF not exist %JPS% (
        set JPS="jps.exe"
        set ERRORLEVEL=0
        "%JPS%" 2>NUL
        IF "%ERRORLEVEL%" NEQ "0" (
            echo jps could not be found in your system, configure your PATH to find java executable or define JAVA_HOME environment variable
            goto eof
        )
)

call :find_process_by_cmd SchedulerStarter
IF NOT [%_result%]==[] (
        echo Error: scheduler is already running on this machine
        goto :eof
)

set SCHED_JVM_X_OPTS=-Xms128m -Xmx2048m
set SCHED_JVM_D_OPTS=
set DROP_DB=
set SCHED_PARAMS=

set VERBOSE=false

set DO_RM=true
set DO_SCHED=true

:ParamParseLoop
IF [%1]==[] goto :ParamParseEndLoop
        IF %1 == -h ( 
                call :print_help
                goto :eof
        ) 
    IF %1 == -v (
        set VERBOSE=true
        goto :ParamParseShift
    ) 
    IF %1 == -u (
        IF [%2]==[] (
            echo -u requires an argument
            goto :eof
        )
        set SCHED_PARAMS=%SCHED_PARAMS% %1 %2
        set RM_URL=%2
        SHIFT
        goto :ParamParseShift
    ) 
    IF %1 == -p (
        IF [%2]==[] (
            echo -p requires an argument
            goto :eof
        )
        set SCHED_PARAMS=%SCHED_PARAMS% %1 %2
        SHIFT
        goto :ParamParseShift
    ) 
    IF %1 == -c (
        set DROP_DB=-Dpa.scheduler.db.hibernate.dropdb=true -Dpa.rm.db.hibernate.dropdb=true
        goto :ParamParseShift
    ) 
    IF %1 == -P (
        IF [%2]==[] (
            echo -P requires an argument
            goto :eof
        )
        set PORT=%2
        SHIFT
        goto :ParamParseShift
    ) 

    set TMP_VAR=%~1
    IF %TMP_VAR:~0,2% == -D (
        set SCHED_JVM_D_OPTS="%TMP_VAR%" %SCHED_JVM_D_OPTS%    
        goto :ParamParseShift
    )

        echo Error: Unknown option %1
        goto :eof

:ParamParseShift
    SHIFT
goto :ParamParseLoop
:ParamParseEndLoop

set SCHED_OUT=SchedulerStarter.output

call init.bat scheduler-log4j-server
echo %JAVA_CMD% %SCHED_JVM_D_OPTS% org.ow2.proactive.scheduler.util.SchedulerStarter > run.tmp.bat
echo EXIT >> run.tmp.bat

echo Starting the scheduler (starter output is redirected to the %SCHED_OUT%)
START /B "Scheduler" run.tmp.bat > %SCHED_OUT%

call :find_process_by_cmd SchedulerStarter
IF [%_result%]==[] (
        echo Error: failed to find Scheduler process PID
        goto :eof
) else (
        set SCHED_PID=%_result%
)

set RM_MATCH=The resource manager with 4 local nodes created on 
set SCHED_MATCH=The scheduler created on 
set SCHED_URL_STR=
set RM_URL_STR=
:WaitLoop
        for /f "tokens=*" %%i in ('FINDSTR /C:"%RM_MATCH%" %SCHED_OUT%') do set RM_URL_STR=%%i
        IF NOT ["%RM_URL_STR%"]==[""] (
                 SET RM_URL=!RM_URL_STR:%RM_MATCH%=! 
        )

        for /f "tokens=*" %%i in ('FINDSTR /C:"%SCHED_MATCH%" %SCHED_OUT%') do set SCHED_URL_STR=%%i
        IF ["%SCHED_URL_STR%"]==[""] (
                echo Waiting for the scheduler...
                call :delay 5 
        ) ELSE (
                SET SCHED_URL=!SCHED_URL_STR:%SCHED_MATCH%=!
                GOTO :EndWaitLoop
        )

        call :find_process_by_pid %SCHED_PID%
        IF [%_result%]==[] (
                echo Error: Scheduler process has terminated
                goto :eof
        )

        GOTO :WaitLoop
:EndWaitLoop

goto :eof

:find_process_by_pid 
        SETLOCAL
        set COMMAND_PID=
        set result=
        %JPS% | findstr /b /l %1% > tmpFile
        SET /p COMMAND_PID=<tmpFile
        IF NOT ["%COMMAND_PID%"]==[""] (
            SET result=OK
        ) ELSE (
            SET result=
        )
        DEL tmpFile
        ENDLOCAL & SET _result=%result%
        goto :eof

:find_process_by_cmd 
        SETLOCAL
        set COMMAND_PID=
        set result=
        %JPS% | findstr %1% > tmpFile
        SET /p COMMAND_PID=<tmpFile
        IF NOT ["%COMMAND_PID%"]==[""] (
            SET result=%COMMAND_PID:~0,-17%
        ) ELSE (
            SET result=
        )
        DEL tmpFile
        ENDLOCAL & SET _result=%result%
        goto :eof

:print_help
        echo scheduler-start-gui.bat [options]
        echo.
    echo If no option is specified, starts a new Resource Manager, starts a new Scheduler,
    echo starts the REST API Server and the Web Interface.
    echo.
    echo -u            Connect to an existing RM instead of starting a new one
    echo -p POLICY     Complete name of the Scheduling policy to use
    echo -c            Start the Scheduler server with a clean Database
    echo -v            Verbose output
    echo -h            Print this message and exit
    echo -Dxxx         JVM option for the RM, Scheduler and HTTP servers, 
    echo               If option contains = then double quotes should be used, ie "-Dproactive.communication.protocol=pnp"
    echo.
        goto :eof

:delay
        ping -n %1 127.0.0.1 > NUL
        goto :eof

:eof
ENDLOCAL