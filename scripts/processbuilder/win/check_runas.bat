@echo off
:: This script checks if the current user can execute as an other user
:: There are two ways of checking:
::	- with just a user name, in this case runas with /savecred is used
::	- with both a username and a password, and in this case a login is attempted
::
:: On success it will display the user name in the last line of the output.

:: Arguments:
::	1 - username
::  2 - password
::  3 - path to the scripts folder (necessary only if the password is set) (SPACE replaced with ?)
IF NOT x%2==x GOTO :withpass

:nopass
echo 0 | runas /user:%1 /noprofile /savecred "cmd.exe /c exit"
IF %ERRORLEVEL% == 0 echo %1
exit 0

:withpass
set path="%3"
IF NOT EXIST "%path:?= %" (echo %OSPL_E_PREFIX% java.io.IOException %OSPL_E_CAUSE% error=2, No such folder "%path:?= %" 1>&2) && (exit 0)
cd /d "%path:?= %"
PsExec.exe -u %1 -p %2 cmd.exe /c exit
IF %ERRORLEVEL% == 0 echo %1
