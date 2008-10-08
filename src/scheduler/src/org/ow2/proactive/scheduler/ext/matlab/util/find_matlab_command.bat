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
FOR /F "usebackq skip=2 tokens=1,3 delims=	" %%i in ( `REG QUERY "HKEY_LOCAL_MACHINE\SOFTWARE\Mathworks\MATLAB" /s` ) DO (
	rem ************** We look at the right key among the results, we exit at the first Matlab instance ***********************
	IF "%%i" == "    MATLABROOT" (
		echo %%j
		break
	)
)
echo ---------------
echo bin\win32\matlab.exe
echo bin\win32
rem *** retrieve the Matlab version
FOR /F "usebackq skip=2 tokens=1,2,3,4,5 delims=\" %%i in ( `REG QUERY "HKEY_LOCAL_MACHINE\SOFTWARE\Mathworks\MATLAB" /s` ) DO (
	rem ************** We look at the right key among the results, we exit at the first Matlab instance ***********************
	IF "%%m" NEQ "" (
		echo %%m
		break
	)
)
echo ---------------
ENDLOCAL
