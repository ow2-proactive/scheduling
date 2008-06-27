#!/bin/sh

echo
echo --- SCHEDULER STRESS TEST ---------------------------------------------

echo shedulerTester [schedulerURL] [jobsFolder] [MaxSubmissionPeriod] [MaxNbJobs]

CLASSPATH=.

workingDir=`pwd`
. ./env.sh
echo $JAVACMD

$JAVACMD org.ow2.proactive.scheduler.examples.SchedulerTester $@

echo

