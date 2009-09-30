@echo off
rem This scripts is used to find Matlab on a remote host.
rem it prints out three information, each info is printed on one line
rem 1) path to the root directory of matlab
rem 2) path to the matlab command (relative from matlab root)
rem 3) path to matlab libraries (relative from matlab root)
rem 4) matlab version (major.minor i.e. 7.1 7.2 7.3 etc...)



SETLOCAL ENABLEDELAYEDEXPANSION



rem ************* We look at the Registry key where the Path to Matlab is stored *************
rem *** retrieve the Matlab command

FOR /F "usebackq skip=2 tokens=1 delims=" %%i in ( `REG QUERY "HKEY_LOCAL_MACHINE\SOFTWARE\Mathworks\MATLAB" /s` ) DO (
	set MATLABKEY=%%i
	set MATLABKEY=!MATLABKEY:REG_SZ=#!
	FOR /F "tokens=1,2 delims=#" %%S in ("!MATLABKEY!") do (
		set MATLABROOT=%%T
		set MATLABROOT=!MATLABROOT:~4!
			
	)
	if NOT "!MATLABROOT!" == "" (
		echo !MATLABROOT!
		goto OUT
    )
)
:OUT

echo ---------------

FOR /F "usebackq" %%a in ( `DIR /B /aD "%MATLABROOT%bin"` ) DO (
	set SUBDIR=%%a
	set SUBDIR=!SUBDIR:~0,3!
	if "!SUBDIR!" == "win" (
		echo bin\%%a\matlab.exe
		echo bin\%%a
	)
	
)

rem echo bin\win32\matlab.exe
rem echo bin\win32
rem *** retrieve the Matlab version
FOR /F "usebackq  tokens=1 delims=" %%a in ( `REG QUERY "HKEY_LOCAL_MACHINE\SOFTWARE\Mathworks\MATLAB" /s` ) DO (
	rem ************** We look at the right key among the results, we exit at the first Matlab instance ***********************	
	set MATLABKEY=%%a
	set MATLABKEY=!MATLABKEY:\=#!	
	FOR /F "tokens=1,2,3,4,5 delims=#" %%i in ("!MATLABKEY!") do (
		set VERSION=%%m
	)
	
	IF NOT "!VERSION!" == "" (
	    echo !VERSION!
		goto OUT2
		break
	)
)
:OUT2
echo ---------------
ENDLOCAL
