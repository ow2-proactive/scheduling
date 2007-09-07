@echo off
rem ----------------------------------------------------------------------------
rem
rem This variable should be set to the directory where is installed ProActive
rem

IF NOT DEFINED PROACTIVE set PROACTIVE=%CD%\..\..

rem ----------------------------------------------------------------------------


if NOT DEFINED JAVA_HOME goto javahome
if "%JAVA_HOME%" == "" goto javahome

rem ----
rem Set up the classpath using classes dir or jar files
rem 

IF DEFINED CLASSPATHEXT (
SET CLASSPATH=%CLASSPATHEXT%
) ELSE (
SET CLASSPATH=.
)

IF EXIST "%PROACTIVE%\classes" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\classes\Core;%PROACTIVE%\classes\Extensions;%PROACTIVE%\classes\Extra;%PROACTIVE%\classes\Examples;%PROACTIVE%\classes\IC2D-old
IF EXIST "%PROACTIVE%\ProActive.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\ProActive.jar
IF EXIST "%PROACTIVE%\ProActive_examples.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\ProActive_examples.jar
IF EXIST "%PROACTIVE%\lib\log4j.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\log4j.jar
IF EXIST "%PROACTIVE%\lib\bouncycastle.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\bouncycastle.jar
IF EXIST "%PROACTIVE%\lib\xercesImpl.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\xercesImpl.jar
IF EXIST "%PROACTIVE%\lib\fractal.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\fractal.jar
IF EXIST "%PROACTIVE%\lib\ganymed-ssh2-build210.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ganymed-ssh2-build210.jar
IF EXIST "%PROACTIVE%\ic2d.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\ic2d.jar
IF EXIST "%PROACTIVE%\lib\javassist.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\javassist.jar

rem -------------------------------------------------
rem jars for Globus
rem -------------------------------------------------
IF EXIST "%PROACTIVE%\lib\cog-jglobus.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\cog-jglobus.jar
IF EXIST "%PROACTIVE%\lib\cog-ogce.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\cog-ogce.jar
IF EXIST "%PROACTIVE%\lib\cryptix.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\cryptix.jar
IF EXIST "%PROACTIVE%\lib\cryptix32.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\cryptix32.jar
IF EXIST "%PROACTIVE%\lib\cryptix-asn1.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\cryptix-asn1.jar
IF EXIST "%PROACTIVE%\lib\puretls.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\puretls.jar

rem -------------------------------------------------
rem jars for Ibis
rem -------------------------------------------------
IF EXIST "%PROACTIVE%\lib.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib.jar

rem -------------------------------------------------
rem jars for Fractal GUI
rem -------------------------------------------------
IF EXIST "%PROACTIVE%\lib\fractal-adl.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\fractal-adl.jar
IF EXIST "%PROACTIVE%\lib\dtdparser.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\dtdparser.jar
IF EXIST "%PROACTIVE%\lib\asm-2.2.1.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\asm-2.2.1.jar
IF EXIST "%PROACTIVE%\lib\ow_deployment_scheduling.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ow_deployment_scheduling.jar
IF EXIST "%PROACTIVE%\lib\fractal-gui.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\fractal-gui.jar
IF EXIST "%PROACTIVE%\lib\fractal-swing.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\fractal-swing.jar
IF EXIST "%PROACTIVE%\lib\julia-asm.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\julia-asm.jar
IF EXIST "%PROACTIVE%\lib\julia-mixins.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\julia-mixins.jar
IF EXIST "%PROACTIVE%\lib\julia-runtime.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\julia-runtime.jar
IF EXIST "%PROACTIVE%\lib\SVGGraphics.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\SVGGraphics.jar

rem -------------------------------------------------
rem jars for Web Services
rem -------------------------------------------------
IF EXIST "%PROACTIVE%\lib\soap.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\soap.jar
IF EXIST "%PROACTIVE%\libdl4j.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\libdl4j.jar
IF EXIST "%PROACTIVE%\lib\axis.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\axis.jar
IF EXIST "%PROACTIVE%\lib\jaxrpc.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\jaxrpc.jar
IF EXIST "%PROACTIVE%\lib\activation.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\activation.jar
IF EXIST "%PROACTIVE%\lib\saaj-api.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\saaj-api.jar
IF EXIST "%PROACTIVE%\lib\commons-logging.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\commons-logging.jar
IF EXIST "%PROACTIVE%\lib\commons-discovery.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\commons-discovery.jar
IF EXIST "%PROACTIVE%\lib\mail.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\mail.jar
IF EXIST "%PROACTIVE%\lib\xml-apis.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\xml-apis.jar

rem -------------------------------------------------
rem jars for Web Services
rem -------------------------------------------------
IF EXIST "%PROACTIVE%\lib\soap.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\soap.jar
IF EXIST "%PROACTIVE%\libdl4j.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\libdl4j.jar
IF EXIST "%PROACTIVE%\lib\axis.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\axis.jar
IF EXIST "%PROACTIVE%\lib\jaxrpc.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\jaxrpc.jar
IF EXIST "%PROACTIVE%\lib\activation.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\activation.jar
IF EXIST "%PROACTIVE%\lib\saaj-api.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\saaj-api.jar
IF EXIST "%PROACTIVE%\lib\commons-logging.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\commons-logging.jar
IF EXIST "%PROACTIVE%\lib\commons-discovery.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\commons-discovery.jar
IF EXIST "%PROACTIVE%\lib\mail.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\mail.jar
IF EXIST "%PROACTIVE%\lib\xml-apis.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\xml-apis.jar

rem -------------------------------------------------
rem jars for TimIt
rem -------------------------------------------------
IF EXIST "%PROACTIVE%\lib\batik-awt-util.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\batik-awt-util.jar
IF EXIST "%PROACTIVE%\lib\batik-dom.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\batik-dom.jar
IF EXIST "%PROACTIVE%\lib\batik-svggen.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\batik-svggen.jar
IF EXIST "%PROACTIVE%\lib\batik-util.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\batik-util.jar
IF EXIST "%PROACTIVE%\lib\batik-xml.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\batik-xml.jar
IF EXIST "%PROACTIVE%\lib\commons-cli-1.0.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\commons-cli-1.0.jar
IF EXIST "%PROACTIVE%\lib\jcommon-1.0.6.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\jcommon-1.0.6.jar
IF EXIST "%PROACTIVE%\lib\jdom.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\jdom.jar
IF EXIST "%PROACTIVE%\lib\jfreechart-1.0.2.jar" set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\jfreechart-1.0.2.jar



set JAVA_CMD="%JAVA_HOME%\bin\java.exe" -Djava.security.manager -Djava.security.policy="%PROACTIVE%\scripts\proactive.java.policy" -Dlog4j.configuration=file:"%PROACTIVE%\scripts\proactive-log4j"

rem Adding java tools to the path
SET OK=1
FOR /F "delims=;" %%i IN ("%PATH%") DO (
IF /I "%%i" == "%JAVA_HOME%\bin" SET OK=0
)
IF /I %OK%==1 SET PATH=%JAVA_HOME%\bin;%PATH%

goto end


:javahome
echo.
echo The enviroment variable JAVA_HOME must be set the current jdk distribution
echo installed on your computer. 
echo Use 
echo    set JAVA_HOME=<the directory where is the JDK>
goto end

 
:end
