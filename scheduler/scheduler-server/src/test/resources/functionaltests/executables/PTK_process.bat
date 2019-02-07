echo Process tree killer test : detached command

cd %~dp0

echo Running in : %CD%

start /B cmd.exe /C TestSleep.exe > NUL 2>&1
