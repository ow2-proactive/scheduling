echo Process tree killer test : detached commands launcher

start /B cmd.exe /C PTK_process2.bat
start /B cmd.exe /C PTK_process2.bat
start /B cmd.exe /C PTK_process2.bat
start /B cmd.exe /C PTK_process2.bat

echo waiting for 10 sec
ping -n 10 127.0.0.1
