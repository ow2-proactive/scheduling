@echo off
echo. 

SETLOCAL
IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\..\.

call %PROACTIVE%\scripts\windows\init.bat

%JAVA_CMD%  -Xmx256000000 -classpath %CLASSPATH%;"%JAVA_HOME%\lib\tools.jar";"%PROACTIVE%\compile\ant.jar";"%PROACTIVE%\compile\ant-launcher.jar";"%PROACTIVE%\compile\xml-apis.jar";"%PROACTIVE%\compile\xercesImpl.jar" org.apache.tools.ant.Main -buildfile %PROACTIVE%\src\org\objectweb\proactive\examples\pi\scripts\build.xml %*
ENDLOCAL