SETLOCAL enabledelayedexpansion
call ..\..\..\scripts\windows\init.bat

echo.
echo --- StartP2PService -------------------------------------

%JAVA_CMD% org.objectweb.proactive.p2p.service.StartP2PService %*

echo.

echo ---------------------------------------------------------