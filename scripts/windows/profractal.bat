@echo off
echo. 

SETLOCAL
call init.bat
IF NOT DEFINED PROFRACTALIB set PROFRACTALIB=%PROACTIVE%\lib\examples
set CLASSPATH=%CLASSPATH%;%PROFRACTALIB%\jai_imageio.jar;%PROFRACTALIB%\mlibwrapper_jai.jar;%PROFRACTALIB%\clibwrapper_jiio.jar;%PROFRACTALIB%\jai_codec.jar;%PROFRACTALIB%\jai_core.jar
%JAVA_CMD% org.objectweb.proactive.examples.profractal.fractParallel profractalrsrcs

ENDLOCAL

echo. 
echo ---------------------------------------------------------
