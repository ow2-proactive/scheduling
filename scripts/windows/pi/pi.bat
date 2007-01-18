@echo off
echo. 

rem --- Verifying current directory
SET COMMAND=%0
IF NOT "%COMMAND:~-4%" == ".bat" (
 SET COMMAND=%0.bat
)
 
SET OK=0
FOR /F %%i in ('dir /b') do IF "%%i" == "%COMMAND%" SET OK=1

IF %OK% == 0 (
echo scripts must be started in the same directory as the script.
goto end
)

SETLOCAL
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..\..

set CLASSPATHEXT=%JAVA_HOME%\lib\tools.jar;%PROACTIVE%\compile\ant.jar;%PROACTIVE%\compile\ant-launcher.jar;%PROACTIVE%\lib\ws\xml-apis.jar;%PROACTIVE%\lib\xercesImpl.jar

call "%PROACTIVE%\scripts\windows\init.bat"
echo on
%JAVA_CMD%  -Xmx256000000 org.apache.tools.ant.Main -buildfile "%PROACTIVE%\src\org\objectweb\proactive\examples\pi\scripts\build.xml" %*
ENDLOCAL