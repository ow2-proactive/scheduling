@echo off
echo.

rem I did not use init.bat because it does not work, and I have no idea why

set PROACTIVE=%~dp0\..\..

set JAVA=%JAVA_HOME%\bin\java.exe
if NOT DEFINED JAVA_HOME set JAVA=java.exe
if "%JAVA_HOME%" == "" set JAVA=java.exe

set ERRORLEVEL=0
"%JAVA%" -version 2>NUL
IF "%ERRORLEVEL%" NEQ "0" (
    echo Java could not be found in your system, configure your PATH to find java executable or define JAVA_HOME environment variable
    goto end
)

IF DEFINED CLASSPATHEXT (
	SET CLASSPATH=%CLASSPATHEXT%
) ELSE (
	SET CLASSPATH=.
)

SET CLASSPATH=%CLASSPATH%;%PROACTIVE%\dist\lib\*

set JAVA_CMD="%JAVA%" -Dproactive.home="%PROACTIVE%" -Dproactive.configuration="%PROACTIVE%\config\proactive\ProActiveConfiguration.xml" -Djava.security.manager -Djava.security.policy="%PROACTIVE%\config\security.java.policy-client"
%JAVA_CMD% org.objectweb.proactive.extensions.vfsprovider.gui.ServerBrowser %*

goto end


echo.

:end
