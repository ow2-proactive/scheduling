REM %1 number of booked cores
SETLOCAL ENABLEdelayedExpansion

SET /a retour=0

SET /a TEST_RES=0

if !%LONG_PAS_CORE==! (
	echo "Error 'PAS_CORE_NB' env variable is not defined"
	SET /a TEST_RES=1
)

if NOT "%PAS_CORE_NB%" == "%1" (
	echo "Error : number of booked host is not $1"
	SET /a TEST_RES=1
)

if !%LONG_PAS_NODEFILE==! (
	echo "Error 'PAS_NODEFILE' env variable is not defined"
	SET /a TEST_RES=1
)


if NOT EXIST %PAS_NODEFILE% (
	echo "Error cannot read 'PAS_NODEFILE'"
	SET /a TEST_RES=1
)

SET /a Compt=0
if EXIST %PAS_NODEFILE% (
	FOR /f "delims=" %%i in (%PAS_NODEFILE%) DO (SET /a Compt=!Compt!+1)
	if NOT "!Compt!" == "%1" (
		echo "Error 'PAS_NODEFILE' must have $1 lines, it has $NBS_LINES"
		type %PAS_NODEFILE%
		SET /a TEST_RES=1
	)
	if "!Compt!" == "%1" (
		goto Check_node_file
		:Suite
		SET /a TEST_RES=%retour%
	)
)

exit %TEST_RES%

:Check_node_file 
SET /a retour=0
GOTO Suite
REM TODO : this line causes the test to end without checking file content as it is not suitable today.
REM this function should be rewritten when test will be distributed
TYPE %PAS_NODEFILE%
setlocal
set filename=ipresult.txt
ipconfig>%filename%
set ip=
set tmpfile=c:\tmp.txt
FIND "IP Address" <%filename%> %tmpfile%
set /p ip= < "%tmpfile%"
del %tmpfile%
del %filename%
set HOST_IP=%ip:~44,15%
for /f "delims=" %%i in (%PAS_NODEFILE%) do (
SET NODE_IP=%%i 
if NOT "%%i" == "%HOST_IP%" GOTO Error
)
SET /a retour=0
GOTO Suite

:Error
echo "Error booked host ip is invalid : %NODE_IP%, awaited : %HOST_IP%"
SET /a retour=1
GOTO Suite
