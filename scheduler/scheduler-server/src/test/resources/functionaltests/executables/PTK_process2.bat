echo Process tree killer test : detached command

cd %~dp0

echo Running in : %CD%

echo waiting for 5 sec
ping -n 5 127.0.0.1
