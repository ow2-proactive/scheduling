@echo off
echo. 
echo --- FlowShop ----------------------------------------

goto doit

:usage
echo. 
echo flowshop.bat 
goto end


:doit
SETLOCAL
call init.bat

%JAVA_CMD% org.objectweb.proactive.examples.flowshop.Main -bench ..\..\src\org\objectweb\proactive\examples\flowshop\taillard\test_10_10.txt -desc ..\..\descriptors\Workers.xml

ENDLOCAL

:end
pause
echo. 
echo ---------------------------------------------------------