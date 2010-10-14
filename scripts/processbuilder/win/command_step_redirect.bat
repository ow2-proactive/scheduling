@ECHO off
::TODO comm
set mydir=%1
set workdir=%2
set envfile=%3
set outpipe=%4o
set errpipe=%4e

%mydir%\command_step.bat %workdir% %envfile% 1>%outpipe% 2>%errpipe%