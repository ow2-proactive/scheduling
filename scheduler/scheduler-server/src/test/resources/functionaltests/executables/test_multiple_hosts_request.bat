REM %1 number of booked cores
SETLOCAL ENABLEdelayedExpansion

SET /a retour=0

SET /a TEST_RES=0

if NOT "%variables_PA_NODESNUMBER%" == "%1" (
	echo "Error : number of booked host is not $1"
	SET /a TEST_RES=1
)

if NOT EXIST %variables_PA_NODESFILE% (
	echo "Error cannot read 'PA_NODEFILE'"
	SET /a TEST_RES=1
)

exit %TEST_RES%