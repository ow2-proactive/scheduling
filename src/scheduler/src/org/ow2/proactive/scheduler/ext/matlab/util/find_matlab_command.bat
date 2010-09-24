@echo off
rem This scripts is used to find Matlab on a remote host.
rem it prints out three information, each info is printed on one line
rem 1) path to the root directory of matlab
rem 2) path to the matlab command (relative from matlab root)
rem 3) path to matlab libraries (relative from matlab root)
rem 4) matlab version (major.minor i.e. 7.1 7.2 7.3 etc...)



SETLOCAL ENABLEDELAYEDEXPANSION



rem ************* We look at the Registry key where the Path to Matlab is stored *************
rem ************** We look at the right key among the results, we exit at the last Matlab instance ***********************
rem *** retrieve the Matlab command

rem ********* building the array **************
set begin=0
set max=0
FOR /F "usebackq tokens=1 delims=" %%i in ( `REG QUERY "HKEY_LOCAL_MACHINE\SOFTWARE\Mathworks\MATLAB" /s` ) DO (
        set LINE=%%i
        set LINE=!LINE:~0,45!
        if "!begin!" == "0" (
		if "!LINE!" == "HKEY_LOCAL_MACHINE\SOFTWARE\Mathworks\MATLAB\" (
		   set begin=1
                   set /a max=1
                   set _!max!=%%i                   
        	)
        ) else ( 
	    if "!LINE!" == "HKEY_LOCAL_MACHINE\SOFTWARE\Mathworks\MATLAB\" (		   
                   set /a max=1
                   set _!max!=%%i                   
            ) else (
            	set /a max=!max!+1
            	set _!max!=%%i	
	    	rem GOTO OUT1    
	    )
	)
)

:OUT1

rem for /l %%a in (1 1 %max%) do echo !_%%a!

FOR /F "tokens=1 delims=" %%i in ( "!_2!" ) DO (
	set MATLABKEY=%%i
	set MATLABKEY=!MATLABKEY:REG_SZ=#!
	FOR /F "tokens=1,2 delims=#" %%S in ("!MATLABKEY!") do (
		set MR=%%T
                
		set MR=!MR:~1!
			
	)
	if NOT "!MR!" == "" (
		rem *** stripping spaces and tabs ***
		
		goto OUT2
    )
)

:OUT2
rem if "%MR:~-1%"==" " set MR=%MR:~0,-1%
rem remove trailing spaces and tabs
for /l %%a in (1,1,31) do if "!MR:~-1!"==" " set MR=!MR:~0,-1!
for /l %%a in (1,1,31) do if "!MR:~-1!"=="	" set MR=!MR:~0,-1!
rem remove beginning spaces and tabs
for /f "tokens=* delims= " %%a in ("%MR%") do set MR=%%a
for /f "tokens=* delims=	" %%a in ("%MR%") do set MR=%%a
set MATLABROOT=!MR!
echo !MATLABROOT!

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
rem FOR /F "usebackq  tokens=1 delims=" %%a in ( `REG QUERY "HKEY_LOCAL_MACHINE\SOFTWARE\Mathworks\MATLAB" /s` ) DO (
FOR /F "tokens=1 delims=" %%a in ( "!_1!" ) DO (
	rem ************** We look at the right key among the results, we exit at the last Matlab instance ***********************	
	set MATLABKEY=%%a
	set MATLABKEY=!MATLABKEY:\=#!	
	FOR /F "tokens=1,2,3,4,5 delims=#" %%i in ("!MATLABKEY!") do (
		set VV=%%m
	)
        rem *** stripping spaces and tabs ***
	FOR /F "tokens=1" %%i in ("!VV!") do (
		set VERSION=%%i
	)
	rem for /l %%a in (1,1,31) do (
	rem 	if "!VERSION:~-1!"==" " set VERSION=!VERSION:~0,-1!
	rem	if "!VERSION:~-1!"=="	" set VERSION=!VERSION:~0,-1!
	rem)
	IF NOT "!VERSION!" == "" (
	    echo !VERSION!
		goto OUT2
		break
	)
)
:OUT2
echo ---------------
ENDLOCAL
