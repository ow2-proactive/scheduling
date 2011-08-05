SET file=%0
SET DIR=%file%\..

echo Process tree killer test : detached commands launcher

start /B cmd /C "%DIR%\PTK_process.bat"
start /B cmd /C "%DIR%\PTK_process.bat"
start /B cmd /C "%DIR%\PTK_process.bat"
start cmd /C "%DIR%\PTK_process.bat"