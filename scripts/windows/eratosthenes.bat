@echo off
echo. 
echo --- Eratosthenes ----------------------------------------
echo. You may pass an XML Deployment Descriptor file as first parameter
echo. An example can be found in ProActive/descriptors/Eratosthenes.xml

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat
%JAVA_CMD% org.objectweb.proactive.examples.eratosthenes.Main %*
ENDLOCAL

echo. 
echo ---------------------------------------------------------
