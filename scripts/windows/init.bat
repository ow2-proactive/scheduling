rem ----------------------------------------------------------------------------
rem
rem This variable should be set to the directory where is installed ProActive
rem

IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\.

rem ----------------------------------------------------------------------------


if NOT DEFINED JAVA_HOME goto javahome
if "%JAVA_HOME%" == "" goto javahome

rem ----
rem Set up the classpath using classes dir or jar files
rem 
set CLASSPATH=.
IF EXIST %PROACTIVE%\classes set CLASSPATH=%CLASSPATH%;%PROACTIVE%\classes
IF EXIST %PROACTIVE%\ProActive.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\ProActive.jar
IF EXIST %PROACTIVE%\lib\bcel.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\bcel.jar
IF EXIST %PROACTIVE%\lib\asm.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\asm.jar
IF EXIST %PROACTIVE%\ProActive_examples.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\ProActive_examples.jar
IF EXIST %PROACTIVE%\ic2d.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\ic2d.jar

IF EXIST %PROACTIVE%\lib\jini-core.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\jini-core.jar
IF EXIST %PROACTIVE%\lib\jini-ext.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\jini-ext.jar
IF EXIST %PROACTIVE%\lib\reggie.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\reggie.jar

rem Globus jar files
IF EXIST %PROACTIVE%\lib\cog-jglobus.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\cog-jglobus.jar
IF EXIST %PROACTIVE%\lib\cog-ogce.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\cog-ogce.jar
IF EXIST %PROACTIVE%\lib\cryptix.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\cryptix.jar
IF EXIST %PROACTIVE%\lib\cryptix32.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\cryptix32.jar
IF EXIST %PROACTIVE%\lib\cryptix-asn1.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\cryptix-asn1.jar
IF EXIST %PROACTIVE%\lib\puretls.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\puretls.jar

IF EXIST %PROACTIVE%\lib\log4j-core.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\log4j-core.jar
IF EXIST %PROACTIVE%\lib\ibis.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ibis.jar
IF EXIST %PROACTIVE%\lib\xercesImpl.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\xercesImpl.jar
IF EXIST %PROACTIVE%\lib\fractal.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\fractal.jar
IF EXIST %PROACTIVE%\lib\bouncycastle.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\bouncycastle.jar
echo CLASSPATH=%CLASSPATH%

set JAVA_CMD=%JAVA_HOME%\bin\java.exe -Djava.security.manager -Djava.security.policy=proactive.java.policy -Dlog4j.configuration=file:proactive-log4j
set PATH=%JAVA_HOME%\bin;%PATH%
goto end


:javahome
echo.
echo The enviroment variable JAVA_HOME must be set the current jdk distribution
echo installed on your computer.
echo Use 
echo    set JAVA_HOME=<the directory where is the JDK>
goto end

 
:end
