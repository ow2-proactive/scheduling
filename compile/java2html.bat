@echo off

if NOT DEFINED JAVA_HOME goto javahome
if "%JAVA_HOME%" == "" goto javahome

SETLOCAL
set docs=..\docs
set CLASSPATH=j2h.jar;..\classes;
echo %CLASSPATH%

echo Converting java to html...
%JAVA_HOME%\bin\java j2h -d %docs%\ProActive_src_html -js ..\src\org\objectweb\proactive -jd %docs%\api http://www-sop.inria.fr/oasis/ProActive/doc/api -t 2 -n "ProActive source code"
rem %JAVA_HOME%\bin\java j2h -d %docs%\examples_src_html -js ..\src\org\objectweb\proactive\examples -jd %docs%\api http://www-sop.inria.fr/oasis/ProActive/doc/api -t 2 -n "ProActive examples source code"
rem %JAVA_HOME%\bin\java j2h -d %docs%\ic2d_src_html -js ..\src\org\objectweb\proactive\ic2d -jd %docs%\api http://www-sop.inria.fr/oasis/ProActive/doc/api  -t 2 -n "IC2D source code"

ENDLOCAL
goto end


:javahome
echo.
echo The enviroment variable JAVA_HOME must be set to the current jdk 
echo distribution installed on your computer.
echo Use 
echo    set JAVA_HOME=<the directory where is the JDK>
goto end


:end
