@echo off
echo.

rem I did not use init.bat because it does not work, and I have no idea why

set PROACTIVE=%~dp0\..\..

if NOT DEFINED JAVA_HOME goto javahome
if "%JAVA_HOME%" == "" goto javahome

IF DEFINED CLASSPATHEXT (
	SET CLASSPATH=%CLASSPATHEXT%
) ELSE (
	SET CLASSPATH=.
)

SET CLASSPATH=%CLASSPATH%;%PROACTIVE%\dist\lib\ProActive.jar;%PROACTIVE%\dist\lib\ProActive_examples.jar;%PROACTIVE%\dist\lib\ProActive_utils.jar

set JAVA_CMD="%JAVA_HOME%\bin\java.exe" -Dproactive.home="%PROACTIVE%" -Dproactive.configuration="file:%PROACTIVE%\config\proactive\ProActiveConfiguration.xml" -Djava.security.manager -Djava.security.policy="%PROACTIVE%\config\security.java.policy-client"
%JAVA_CMD% org.objectweb.proactive.extensions.vfsprovider.gui.ServerBrowser

goto end

:javahome
echo.
echo The environment variable JAVA_HOME must be set the current jdk distribution
echo installed on your computer.
echo Use
echo    set JAVA_HOME=<the directory where is the JDK>
goto end

echo.

:end
