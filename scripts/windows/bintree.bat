@echo off
echo. 
echo --- bintree ---------------------------------------------

goto doit

:usage
echo. 
goto end

:doit

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat
%JAVA_CMD% org.objectweb.proactive.examples.binarytree.TreeApplet
ENDLOCAL

:end
pause
echo. 
echo ---------------------------------------------------------
