@echo off
REM Script used to map the proactive directory to a drive letter (to avoid "command too long" errors)
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

rem ************* We look at the Registry key to find drive letters already used *************
rem "usebackq skip=2 tokens=1,3 delims=	"
SET DRIVES=
FOR /F "usebackq skip=2 tokens=1,2,3 delims=	:\" %%i in ( `REG QUERY "HKEY_LOCAL_MACHINE\SYSTEM\MountedDevices" /s` ) DO (
	rem ************** We look at the right key among the results, we exit at the first Matlab instance ***********************
	IF "%%j" == "DosDevices" (
	SET DRIVES=!DRIVES!%%k
	)
)
SET EVERY=Z Y X W V U T S R Q P O N M L K J I H G F E D C B A

FOR /F %%i IN ("%EVERY%") DO (
	echo %DRIVES%|find "%%i"
	IF ERRORLEVEL 1 (
		SUBST %%i: /D 2>NUL
		SUBST %%i: "%PROACTIVE%"
		SET PROACTIVE=%%i: 
		goto :EOF
	)
)

