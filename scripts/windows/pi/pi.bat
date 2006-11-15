@echo off
echo. 

SETLOCAL
call init.bat
set PROACTIVE=..\..\..\.
%JAVA_CMD%  -Xmx256000000 -classpath %JAVA_HOME%\lib\tools.jar:%PROACTIVE%\compile\ant.jar:%PROACTIVE%\compile\ant-launcher.jar:%PROACTIVE%\compile\xml-apis.jar:%PROACTIVE%\compile\xercesImpl.jar org.apache.tools.ant.Main -buildfile %PROACTIVE%\src\org\objectweb\proactive\examples\pi\scripts\build.xml %*
ENDLOCAL