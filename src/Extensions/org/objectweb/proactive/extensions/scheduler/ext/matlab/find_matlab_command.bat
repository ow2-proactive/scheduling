@echo off
SETLOCAL ENABLEDELAYEDEXPANSION
rem ************* We look at the Registry key where the Path to Matlab is stored *************
rem *** retrieve the Matlab command
FOR /F "usebackq skip=2 tokens=1,3 delims=	" %%i in ( `REG QUERY "HKEY_LOCAL_MACHINE\SOFTWARE\Mathworks\MATLAB" /s` ) DO (
	rem ************** We look at the right key among the results, we exit at the first Matlab instance ***********************
	IF "%%i" == "    MATLABROOT" (
		echo %%jbin\matlab.exe
		break
	)
)
echo bin\win32
rem *** retrieve the Matlab version
FOR /F "usebackq skip=2 tokens=1,2,3,4,5 delims=\" %%i in ( `REG QUERY "HKEY_LOCAL_MACHINE\SOFTWARE\Mathworks\MATLAB" /s` ) DO (
	rem ************** We look at the right key among the results, we exit at the first Matlab instance ***********************
	IF "%%m" NEQ "" (
		echo %%m
		break
	)
)
ENDLOCAL
