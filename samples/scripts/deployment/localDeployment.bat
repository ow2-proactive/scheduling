@echo off
:: Start a ProActive Runtime localy and register it in the Resource Manager.
:: Script will be killed when the node is registered.

:: Script parameters
set HOST_NAME=%1
set NODE_NAME=%2
set NODE_SOURCE_NAME=%3
set RM_URL=%4

:: Script constants
set RM_HOME_NODE=UPDATE_ME
set JAVA_HOME_NODE=UPDATE_ME
set JAVA_OPTS_NODE=-Djava.security.policy=%RM_HOME_NODE%\config\security.java.policy-client -Dproactive.useIPaddress=true -Dlog4j.configuration=%RM_HOME_NODE%\config\log4j\log4j-defaultNode
set CREDENTIALS="UPDATE_ME"

set CLASSPATH=%CLASSPATH%;
set CLASSPATH=%CLASSPATH%;%RM_HOME_NODE%\dist\lib\jruby.jar
set CLASSPATH=%CLASSPATH%;%RM_HOME_NODE%\dist\lib\jython-2.5.4-rc1.jar
set CLASSPATH=%CLASSPATH%;%RM_HOME_NODE%\dist\lib\groovy-all-2.1.5.jar
set CLASSPATH=%CLASSPATH%;%RM_HOME_NODE%\dist\lib\commons-logging-1.1.1.jar
set CLASSPATH=%CLASSPATH%;%RM_HOME_NODE%\dist\lib\ProActive_SRM-common.jar
set CLASSPATH=%CLASSPATH%;%RM_HOME_NODE%\dist\lib\ProActive_ResourceManager.jar
set CLASSPATH=%CLASSPATH%;%RM_HOME_NODE%\dist\lib\ProActive_Scheduler-worker.jar
set CLASSPATH=%CLASSPATH%;%RM_HOME_NODE%\dist\lib\commons-httpclient-3.1.jar
set CLASSPATH=%CLASSPATH%;%RM_HOME_NODE%\dist\lib\commons-codec-1.3.jar
set CLASSPATH=%CLASSPATH%;%RM_HOME_NODE%\dist\lib\ProActive.jar
set CLASSPATH=%CLASSPATH%;%RM_HOME_NODE%\addons

echo "Starting the node"
%JAVA_HOME_NODE%\bin\java %JAVA_OPTS_NODE% org.ow2.proactive.resourcemanager.utils.RMNodeStarter -v %CREDENTIALS% -n %NODE_NAME% -s %NODE_SOURCE_NAME% -p 30000 -r %RM_URL%
