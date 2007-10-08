@echo off
echo. 

SETLOCAL ENABLEDELAYEDEXPANSION
IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..
IF NOT DEFINED PROFRACTALIB set PROFRACTALIB=%PROACTIVE%\lib\examples
set CLASSPATHEXT=%CLASSPATHEXT%;%PROFRACTALIB%\jai_imageio.jar;%PROFRACTALIB%\mlibwrapper_jai.jar;%PROFRACTALIB%\clibwrapper_jiio.jar;%PROFRACTALIB%\jai_codec.jar;%PROFRACTALIB%\jai_core.jar
call init.bat

echo on
%JAVA_CMD% org.objectweb.proactive.examples.profractal.fractParallel profractalrsrcs

ENDLOCAL
pause
echo. 
echo ---------------------------------------------------------
