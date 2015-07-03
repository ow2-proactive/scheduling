for /f "delims=" %%i in ('chdir') do Set WORKING_DIR="%%i"

if NOT "%1" == %WORKING_DIR% (
	echo "working dir %WORKING_DIR% is not the awaited working dir : %1"
	exit 1
)