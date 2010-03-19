#!/bin/sh
# Wrapper script for the Scheduler Job Submission stress test
# Running -h or --help will bring up the full options listing for this test
# This script requires AT LEAST one argument : the URL of the scheduler to be tested. Specify it like this:
# ./job_submit.sh -u $SCHEDULER_URL
# Default arguments for the stress test are supplied, should you need something different
# just modify the DEFAULT_* variables below

CLASSPATH=.

# test JAVA_HOME
JAVA_HOME=${JAVA_HOME-NULL};
if [ "$JAVA_HOME" = "NULL" ]
then
echo
echo "The environment variable JAVA_HOME must be set the current jdk distribution"
echo "installed on your computer."
echo "Use "
echo "    export JAVA_HOME=<the directory where is the JDK>"
exit 127
fi

workingDir=`dirname $0`

#set Scheduler install directory env var
SCHEDULER_HOME=$(cd $workingDir/../../.././ || (echo "Broken Scheduler/Resource Manager installation" ; exit 1) && echo $PWD)

# stress tests config dir
STRESS_TESTS_CONFIG=$(cd $SCHEDULER_HOME/src/scheduler/tests/scalabilityTests/config || (echo "Broken Scheduler/Resource Manager installation" ; exit 1) && echo $PWD)
# stress tests classes location
STRESS_TESTS_CLASSES=$(cd $SCHEDULER_HOME/classes/schedulerTests/ || (echo "Broken Scheduler/Resource Manager installation" ; exit 1) && echo $PWD)

print_jars() {
        find "$1" -name "*.jar" | while read jar; do
            printf "%s:" "$jar"
        done
}

export CLASSPATH=$CLASSPATH:"$STRESS_TESTS_CLASSES":`print_jars "$SCHEDULER_HOME/dist/lib/"`

JAVACMD=$JAVA_HOME"/bin/java -Djava.security.manager -Dproactive.configuration=\"$STRESS_TESTS_CONFIG\"/proactive/ProActiveConfiguration.xml -Djava.security.policy=\"$STRESS_TESTS_CONFIG\"/tests.java.policy -Dlog4j.configuration=file:\"$STRESS_TESTS_CONFIG\"/log4j/tests-log4j -Dproactive.home=\"$SCHEDULER_HOME\" -Dpa.scheduler.home=\"$SCHEDULER_HOME\""

# the Scheduler URL
if [ $# == 0 ]
then
    echo "This script requires AT LEAST one argument : the URL of the scheduler to be tested. Specify it like this:"
    echo "`basename $0` -u \$SCHEDULER_URL"
    exit 42
fi

# some default params for running the tests
DEFAULT_GCMA=$STRESS_TESTS_CONFIG/deployment/GCMA.xml
DEFAULT_VIRTUAL_NODE=Nodes
DEFAULT_LOGIN_FILE=$SCHEDULER_HOME/config/authentication/login.cfg
DEFAULT_JOB=$SCHEDULER_HOME/samples/jobs_descriptors/Job_8_tasks.xml
DEFAULT_ARGS="-ad $DEFAULT_GCMA -vn $DEFAULT_VIRTUAL_NODE -lf $DEFAULT_LOGIN_FILE -j $DEFAULT_JOB -me -jr"

eval $JAVACMD scalabilityTests.scenarios.SchedulerJobSubmission $DEFAULT_ARGS $@
