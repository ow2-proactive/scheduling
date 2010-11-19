@ECHO off
:: last step of the command launching mechanism
:: will test if a command can or can not be launched by the current user

:: parameter:
:: 	$1 - absolute path to the working dir of user command (ATTENTION! Spaces replaced with ?)
::  $2 - path to batch file containing env variables, or "_" if there are none (ATTENTION! Spaces replaced with ?)
::	$3... - command to execute (ATTENTION! Spaces replaced with ? in the first argument (3))

:: IMPORTANT! For information about messages please refer to:
::	* Javadoc of OSProcessbuilder
:: 	* C++ source of the PipeBridge

:: standard header
set OSPL_E_PREFIX=_OS_PROCESS_LAUNCH_ERROR_
set OSPL_E_CAUSE=CAUSE
set OSLP_PACKAGE=org.objectweb.proactive.extensions.processbuilder.exception.
:: ---------------

set envfile=%2

:: extract executable
for /f "tokens=1-3*" %%a in ("%*") do (
	set cmd_coded=%%c
    set therest=%%d
)

:: if no env file then skip this step and proceed with launching
IF %envfile%==_ GOTO :skip_env

FOR /F "tokens=*" %%i in ('type "%envfile:?= %"') do (
	SET %%i
)

:skip_env

:: goto workdir
set workdir=%1
cd /d %workdir:?= %

set cmd=%cmd_coded:?= %
IF EXIST %cmd% GOTO :do_launch

:: find executable in path
for %%a in ("%cmd%") do set found=%%~$PATH:a
IF "%found%"=="" (echo %OSPL_E_PREFIX% java.io.IOException %OSPL_E_CAUSE% error=2, No such file or directory 1>&2) && (exit 0)

:do_launch

:: let's tell the launcher that everything is OK
set confirm=_OS_PROCESS_LAUNCH_INIT_FINISHED_
echo %confirm% 1>&2
echo %confirm%
:: execute it!

cmd.exe /c "%cmd%" "%therest%"
echo _OS_PROCESS_LAUNCHER_EXIT_CODE_ %ERRORLEVEL% >&2


