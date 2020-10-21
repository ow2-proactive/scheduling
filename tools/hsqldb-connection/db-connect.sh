#!/usr/bin/env bash

##############################################################################
##
##  A Simple Shell Command To access HSQLDB CLI and connect by default to scheduler DB
##  If you would like to connect to another ProActive DB, please configure the sqltool.rc file before running the script
##
##############################################################################

SAVED="`pwd`"
cd "`dirname \"$PRG\"`/.." >&-
APP_HOME="`pwd -P`"
cd "$SAVED" >&-

################################################################
# if local JVM exists redefine JAVA_HOME
if [ -d "$APP_HOME/jre" ]; then

    JAVA_HOME=$APP_HOME/jre

    if [ -d "$APP_HOME/jre/Contents/Home" ]; then
        # macos
        JAVA_HOME=$APP_HOME/jre/Contents/Home
    fi
fi
################################################################

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

exec "$JAVACMD" -jar sqltool-2.5.1.jar --rcFile=sqltool.rc scheduler
