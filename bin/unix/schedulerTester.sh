#!/bin/sh

echo
echo --- SCHEDULER STRESS TEST ---------------------------------------------

echo shedulerTester [schedulerURL] [jobsFolder] [MaxSubmissionPeriod] [MaxNbJobs]

workingDir=`pwd`
CLASSPATH=.
. ./env.sh

opt="-Xms128m -Xmx2048m"

echo $JAVACMD

$JAVACMD $opt org.ow2.proactive.scheduler.examples.SchedulerTester $@

echo

