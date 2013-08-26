echo Process tree killer test : detached commands launcher

start /B cmd.exe /C PTK_process.bat
start /B cmd.exe /C PTK_process.bat
start /B cmd.exe /C PTK_process.bat
start /B cmd.exe /C PTK_process.bat

echo waiting for 10 sec
ping -n 10 127.0.0.1
