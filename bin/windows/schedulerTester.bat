@echo off
echo. 
echo --- SCHEDULER STRESS TEST ---------------------------------------------

echo shedulerTester [schedulerURL] [jobsFolder] [MaxSubmissionPeriod] [MaxNbJobs]

SETLOCAL ENABLEDELAYEDEXPANSION
call init.bat

%JAVA_CMD% org.ow2.proactive.scheduler.examples.SchedulerTester %*
ENDLOCAL

:end
echo.
echo ---------------------------------------------------------

