@ECHO off
:: step in the process launching mechanism which will try to execute the command
:: as the user passed as parameter (1).
:: If it fails, it will write an error message to the stdout

:: parameters:
::  $1 - output pipe name (append 'o' for output and 'e' for error)
:: 	$2 - user name (can not be empty)
::	$3 - password switch ("p" for enabled - "_" for disabled) (password is in PA_OSPB_USER_PASSWORD env var)
::	$4 - working dir for user command (absolute) (ATTENTION! Spaces replaced with ?)
::  $5 - batch file containing the env. variables (can be "_" == no variables to load) (ATTENTION! Spaces replaced with ?)
::	$6... - command to execute (ATTENTION! Spaces replaced with ? in the first argument (6))

:: IMPORTANT! For information about messages please refer to:
::	* Javadoc of OSProcessbuilder
:: 	* C++ source of the PipeBridge

:: standard header
set OSPL_E_PREFIX=_OS_PROCESS_LAUNCH_ERROR_
set OSPL_E_CAUSE=CAUSE
set OSLP_PACKAGE=org.objectweb.proactive.extensions.processbuilder.exception.
:: ---------------

set usr=%2
set passw_enabled=%3
set workdir=%4
set outpipe=%1
set envfile=%5

IF NOT EXIST %tmp:?= % (echo %OSPL_E_PREFIX% %OSLP_PACKAGE%FatalProcessBuilderException %OSPL_E_CAUSE% Could not access temp file for storing the return value! 1>&2) && (echo 1 > %outpipe%o) && (echo 1 > %outpipe%e) && (exit 0)
:: if we have a tempfile:

IF X%usr%==X (echo %OSPL_E_PREFIX% %OSLP_PACKAGE%OSUserException %OSPL_E_CAUSE% Username can not be empty! 1>&2) && (echo 1 > %outpipe%o) && (echo 1 > %outpipe%e) && (exit 0)

:: we remain with only the user's command
for /f "tokens=1-5*" %%a in ("%*") do (
    set therest=%%f
)

IF x%passw_enabled%==xp GOTO :use_psexec

(runas /profile /user:%usr% /savecred "%CD%\command_step.bat %workdir% %envfile% 1>%outpipe%o 2>%outpipe%e") || ((echo %OSPL_E_PREFIX% %OSLP_PACKAGE%OSUserException %OSPL_E_CAUSE% Cannot execute as user %usr%! 1>&2) && (echo 1 > %outpipe%o) && (echo 1 > %outpipe%e) && (exit 0))
goto :skip

:use_psexec
set passw=%PA_OSPB_USER_PASSWORD%
set PA_OSPB_USER_PASSWORD=***

:: OPTIONAL - BUT SAFER
:: check if we have the PsExec around
for %%a in ("PsExec.exe") do set foundps=%%~$PATH:a
IF EXIST PsExec.exe set foundps=%foundps%found
IF "%foundps%"=="" (echo %OSPL_E_PREFIX% %OSLP_PACKAGE%FatalProcessBuilderException %OSPL_E_CAUSE% Could not locate PsExec.exe on the system! 1>&2) && (exit 0)
:: END OF OPTIONAL

(PsExec.exe -u %usr% -p "%passw%" %CD%\command_step_redirect.bat %CD% %workdir% %envfile% %outpipe% 2>NUL) || ((echo %OSPL_E_PREFIX% %OSLP_PACKAGE%OSUserException %OSPL_E_CAUSE% Cannot execute as user %usr%! 1>&2) && (echo 1 > %outpipe%o) && (echo 1 > %outpipe%e) && (exit 0))

:skip