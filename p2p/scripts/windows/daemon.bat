setlocal enabledelayedexpansion
call ..\..\..\scripts\windows\init.bat

java -cp %CLASSPATH% -Djava.security.manager -Djava.security.policy=proactive.java.policy -Dlog4j.configuration=proactive-log4j rg.objectweb.proactive.p2p.daemon.Daemon %*
exit %errorlevel%

endlocal
