@ECHO off
:: OSProcessBuilder - launcher script for linux
:: This script will need the following arguments:
:: 	$1 - name of the pipe file used for output forwarding (\\.\pipe\pipe_name)
::  $2 - path to scripts folder (ATTENTION! Spaces replaced with ?)
::	$3 - path to the working dir of the user command (absolute) (ATTENTION! Spaces replaced with ?)
::  $4 - path to the .bat file containing the environment variables (if it is "_" than no environment has to be loaded) (ATTENTION! Spaces replaced with ?)
::	$5 - core binding (can be empty, i.e. "_")
:: 	$6 - user name (can be empty, i.e. "_")
::	$7 - "p" (enable password switch) - anything else means no password (anything else should be "_") (password is coming in the PA_OSPB_USER_PASSWORD env variable)
::	$8... - command to execute (ATTENTION! Spaces replaced with ? in the first argument (8))

:: IMPORTANT! For information about messages please refer to:
::	* Javadoc of OSProcessbuilder
:: 	* C++ source of the PipeBridge

:: standard header
set OSPL_E_PREFIX=_OS_PROCESS_LAUNCH_ERROR_
set OSPL_E_CAUSE=CAUSE
set OSLP_PACKAGE=org.objectweb.proactive.extensions.processbuilder.exception.
:: ---------------
set workdir=%3
IF %workdir%==_ set workdir=%CD%
set scriptdir=%2
set envfile=%4

set crs=%5
set usr=%6

set outpipe=%1


IF NOT %crs%==_ (echo %OSPL_E_PREFIX% %OSLP_PACKAGE%CoreBindingException %OSPL_E_CAUSE% Core binding is not supported! 1>&2) &&(exit 0)

set haspass=%7
:: we remain with only the user's command
for /f "tokens=1-7*" %%a in ("%*") do (
    set therest=%%h
)
cd /D %scriptdir:?= %
(user_step.bat %outpipe% %usr% %haspass% %workdir% %envfile% "%therest%") || ((echo %OSPL_E_PREFIX% %OSLP_PACKAGE%FatalProcessBuilderException %OSPL_E_CAUSE% User change script could not execute! 1>&2) && (echo 1 > %outpipe%o) && (echo 1 > %outpipe%e) && (exit 0))


