echo Process tree killer test : detached commands launcher

cd %~dp0

echo Running in : %CD%

start /B cmd.exe /C PTK_process.bat
start /B cmd.exe /C PTK_process.bat
start /B cmd.exe /C PTK_process.bat
start /B cmd.exe /C PTK_process.bat

echo waiting for 30 sec
ping -n 30 127.0.0.1
