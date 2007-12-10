#!/bin/sh

echo
echo --- SCHEDULER STRESS TEST ---------------------------------------------

echo shedulerTester [schedulerURL] [jobsFolder] [MaxSubmissionPeriod] [MaxNbJobs]

workingDir=..
PROACTIVE=$workingDir/../..
CLASSPATH=.
. $workingDir/env.sh

echo $JAVACMD

$JAVACMD org.objectweb.proactive.extensions.scheduler.examples.SchedulerTester $@

echo

