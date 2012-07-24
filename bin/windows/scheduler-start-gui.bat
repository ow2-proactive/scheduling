@echo off
SETLOCAL

call :find_process_by_cmd SchedulerStarter
IF NOT [%_result%]==[] (
        echo Error: scheduler is already running on this machine
        goto :eof
)

call init.bat scheduler-log4j-server

set SCHED_JVM_X_OPTS=-Xms128m -Xmx2048m
set SCHED_JVM_D_OPTS=
set DROP_DB=
set SCHED_PARAMS=

set SCHED_WAR=%PA_SCHEDULER%\dist\war\scheduler.war
set RM_WAR=%PA_SCHEDULER%\dist\war\rm.war
set REST_WAR=%PA_SCHEDULER%\dist\war\rest.war
set PORT=8080
set VERBOSE=false

set DO_RM=true
set DO_REST=true
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
    IF %1 == -s (
        set DO_SCHED=true
        set DO_RM=false
        set DO_REST=true
        goto :ParamParseShift
    ) 
    IF %1 == -r (
        set DO_SCHED=false
        set DO_RM=true
        set DO_REST=true
        goto :ParamParseShift
    ) 
    IF %1 == -a (
        set DO_SCHED=false
        set DO_RM=false
        set DO_REST=true
        goto :ParamParseShift
    ) 
    IF %1 == -S (
        IF [%2]==[] (
            echo -S requires an argument
            goto :eof
        )
        set SCHED_WAR=%2
        SHIFT
        goto :ParamParseShift
    ) 
    IF %1 == -R (
        IF [%2]==[] (
            echo -R requires an argument
            goto :eof
        )
        set RM_WAR=%2
        SHIFT
        goto :ParamParseShift
    ) 
    IF %1 == -A (
        IF [%2]==[] (
            echo -A requires an argument
            goto :eof
        )
        set REST_WAR=%2
        SHIFT
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
        set SCHED_JVM_D_OPTS=%TMP_VAR% %SCHED_JVM_D_OPTS%    
        goto :ParamParseShift
    )

        echo Error: Unknown option %1
        goto :eof

:ParamParseShift
    SHIFT
goto :ParamParseLoop
:ParamParseEndLoop

IF NOT exist "%REST_WAR%" (
    echo Error: invalid REST API war: %REST_WAR%
    goto :eof
)

IF %DO_SCHED% == true (
    IF NOT exist "%SCHED_WAR%" (
        echo Error: invalid scheduling war: %SCHED_WAR%
        goto :eof
    )
)
IF %DO_RM% == true (
    IF NOT exist "%RM_WAR%" (
        echo Error: invalid RM war: %RM_WAR%
        goto :eof
    )
)

set SCHED_OUT=SchedulerStarter.output

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat scheduler-log4j-server
echo %JAVA_CMD% org.ow2.proactive.scheduler.util.SchedulerStarter > run.tmp.bat
echo EXIT >> run.tmp.bat

echo Starting the scheduler (starter output is redirected to the %SCHED_OUT%)
START /B "Scheduler" run.tmp.bat > %SCHED_OUT%
ENDLOCAL

call :find_process_by_cmd SchedulerStarter
IF [%_result%]==[] (
        echo Error: failed to find Scheduler process PID
        goto :eof
) else (
        set SCHED_PID=%_result%
)

set SCHED_URL_STR=
set RM_URL_STR=
:WaitLoop
        for /f "tokens=*" %%i in ('FINDSTR /C:"The resource manager with 4 local nodes created on" %SCHED_OUT%') do set RM_URL_STR=%%i
        IF NOT ["%RM_URL_STR%"]==[""] (
                SET RM_URL=%RM_URL_STR:~28%
        )

        for /f "tokens=*" %%i in ('FINDSTR /C:"The scheduler created on" %SCHED_OUT%') do set SCHED_URL_STR=%%i
        IF ["%SCHED_URL_STR%"]==[""] (
                echo Waiting for the scheduler...
                call :delay 5 
        ) ELSE (
                SET SCHED_URL=%SCHED_URL_STR:~34%
                GOTO :EndWaitLoop
        )

        call :find_process_by_pid %SCHED_PID%
        IF [%_result%]==[] (
                echo Error: Scheduler process has terminated
                goto :eof
        )

        GOTO :WaitLoop
:EndWaitLoop

set CMD=-A %REST_WAR% -p %PORT%

IF %DO_SCHED% == true (
    IF NOT [%SCHED_URL%]==[] (
        echo Scheduler URL: %SCHED_URL%
    ) ELSE (
        echo Error: Could not determine Scheduler URL
        goto :eof
    )
    set CMD=%CMD% -S %SCHED_WAR% -s %SCHED_URL%
)
IF %DO_RM% == true (
    IF NOT [%RM_URL%]==[] (
        echo RM URL: %RM_URL%
    ) ELSE (
        echo Error: Could not determine RM URL
        goto :eof
    )
    set CMD=%CMD% -R %RM_WAR% -r %RM_URL%
)
IF %VERBOSE% == false (
    set CMD=%CMD% -q
)
echo jetty-launcher.bat %CMD% %SCHED_JVM_D_OPTS%
call jetty-launcher.bat %CMD% %SCHED_JVM_D_OPTS%

goto :eof

:find_process_by_pid 
        SETLOCAL
        set COMMAND_PID=
        rem wmic process where (ProcessId=%1) get CommandLine,ProcessId
        wmic process where (ProcessId=%1) get ProcessId /Value 2>NUL | FIND "=" > tmpFile
        SET /p COMMAND_PID=<tmpFile
        IF NOT ["%COMMAND_PID%"]==[""] (
                SET COMMAND_PID=%COMMAND_PID:~10%
        )
        DEL tmpFile
        ENDLOCAL & SET _result=%COMMAND_PID%
        goto :eof

:find_process_by_cmd 
        SETLOCAL
        set COMMAND_PID=
        rem wmic process where (CommandLine like "%%%*%%" and not CommandLine like "%%wmic%%") get CommandLine,ProcessId /Value
        wmic process where (CommandLine like "%%%*%%" and not CommandLine like "%%wmic%%") get ProcessId /Value 2>NUL | FIND "=" > tmpFile
        SET /p COMMAND_PID=<tmpFile
        IF NOT ["%COMMAND_PID%"]==[""] (
                SET COMMAND_PID=%COMMAND_PID:~10%
        )
        DEL tmpFile
        ENDLOCAL & SET _result=%COMMAND_PID%
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
    echo -s            Start only the Scheduler Web App, not the RM's
    echo -r            Start only the RM Web App, not the Scheduler's
    echo -a            Start the REST API Server only, not the GUI Web Apps
    echo -S PATH       Path to the Scheduler Web Application folder or .war file, default: %SCHED_WAR%
    echo -R PATH       Path to the RM Web Application folder or .war file, default: %RM_WAR%
    echo -A            Path to the REST Server API Web Application folder or .war file, default: %REST_WAR%
    echo -P PORT       HTTP server port for the Web UI. default: %PORT%
    echo -v            Verbose output
    echo -h            Print this message and exit
    echo -Dxxx         JVM option for the RM, Scheduler and HTTP servers, 
    echo               If option contains = then double quotes should be used, ie "-Dproactive.communication.protocol=pnp"
    echo.
        goto :eof

:delay
        choice /T %1 /d y > NUL
        goto :eof

:eof
ENDLOCAL