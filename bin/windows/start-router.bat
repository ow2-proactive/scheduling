@echo off
echo.
echo --- StartRouter----------------------------------------

set ERRORLEVEL=

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat log4j-client
rem Fine GC tuning: 
rem 1- Most object are short lived (messages)
rem 2- Very few objects go to the old generation (client information, few kB per client)
rem 3- Try to avoid stop the world (concurrent mark & sweep, full GC at 50%)
rem 4- It's better to have more slower young gc than a long one
SET JVM_ARGS "-server -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=50 -XX:NewRatio=2 -Xms512m -Xmx512m"

%JAVA_CMD% %JVM_ARGS% org.objectweb.proactive.extensions.pamr.router.Main %*
ENDLOCAL

:end
echo.
echo ---------------------------------------------------------

exit /B %ERRORLEVEL%
