@echo off
echo. 
echo --- Compile ---------------------------------------------

if "%1" == "" goto usage
if NOT DEFINED JAVA_HOME goto javahome
if "%JAVA_HOME%" == "" goto javahome

goto doit


:usage
echo. 
echo Compile one example
echo    - parameter : the name of the example to compile (name of directory)
echo. 
echo ex : compile readers
echo.
echo List of examples :
echo   + algebra
echo   + binarytree
echo   + boundedbuffer
echo   + cruisecontrol
echo   + cs (client - server)
echo   + doctor
echo   + garden
echo   + hello
echo   + penguin
echo   + philosophers
echo   + readers
echo. 
goto end


:doit
SETLOCAL
set PATH=%JAVA_HOME%\bin;%PATH%

FOR /F %%f IN ('dir /B ..\..\src\org\objectweb\proactive\examples\%1\*.jav?') DO call setFile.bat %1 %%f
rem javac.exe -d ..\classes org\objectweb\proactive\examples\%1\%%f

pushd .
cd ..\..\src

set CLASSPATH=.
set PROACTIVE=..\.
IF NOT EXIST %PROACTIVE%\classes mkdir %PROACTIVE%\classes
IF EXIST %PROACTIVE%\classes set CLASSPATH=%CLASSPATH%;%PROACTIVE%\classes
IF EXIST %PROACTIVE%\ProActive.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\ProActive.jar
IF EXIST %PROACTIVE%\ProActive_examples.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\ProActive_examples.jar

echo CLASSPATH=%CLASSPATH%

echo compiling 
echo %FILES_LIST%
%JAVA_HOME%\bin\javac.exe -d ..\classes %FILES_LIST%
popd
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
echo. 
echo -----------------------------------------------------------------
