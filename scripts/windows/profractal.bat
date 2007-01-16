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
IF NOT DEFINED PROFRACTALIB set PROFRACTALIB=%PROACTIVE%\lib\examples
set CLASSPATHEXT=%CLASSPATHEXT%;%PROFRACTALIB%\jai_imageio.jar;%PROFRACTALIB%\mlibwrapper_jai.jar;%PROFRACTALIB%\clibwrapper_jiio.jar;%PROFRACTALIB%\jai_codec.jar;%PROFRACTALIB%\jai_core.jar
call init.bat

echo on
%JAVA_CMD% org.objectweb.proactive.examples.profractal.fractParallel profractalrsrcs

ENDLOCAL

echo. 
echo ---------------------------------------------------------
