echo Process tree killer test : detached commands launcher

cd %~dp0

echo Running in : %CD%

start /B cmd.exe /C PTK_process2.bat
start /B cmd.exe /C PTK_process2.bat
start /B cmd.exe /C PTK_process2.bat
start /B cmd.exe /C PTK_process2.bat

echo waiting for 30 sec
ping -n 30 127.0.0.1
