setlocal enabledelayedexpansion
call ..\init.bat

%JAVA_CMD% org.objectweb.proactive.p2p.daemon.Daemon %*
exit %errorlevel%

endlocal
