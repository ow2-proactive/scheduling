@rem A Simple Shell Command To access HSQLDB CLI and connect by default to scheduler DB
@rem If you would like to connect to another ProActive DB, please configure the sqltool.rc file before running the script
@rem If java is not set on your system, please use java under [Schedulering_Path]/jre/bin/java

java -jar sqltool-2.4.1.jar --rcFile=sqltool.rc scheduler
