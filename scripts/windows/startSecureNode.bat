@echo off
echo. 
echo --- StartNode----------------------------------------
if "%1" == "" goto usage
if "%2" == "" goto usage
if "%3" == "" goto usage
if "%4" == "" goto usage

goto doit

:usage
echo. 
echo Start a new Node
echo    - 1 : the url of the node to create
echo    - 2 : public certificate filename
echo    - 3 : private certificate filename
echo    - 4 : public Key filename
echo. 
echo ex : startNode //localhost/node1 publicCertificate privateCertificate acPublicKey
echo. 
goto end


:doit
SETLOCAL
call init.bat
%JAVA_CMD% org.objectweb.proactive.rmi.StartSecureNode %1 %2 %3 %4 %5 %6 %7 %8 %9
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
