from org.ow2.proactive.scripting.helper.progress import ProgressFile
from org.ow2.proactive.scheduler.task import SchedulerVars

import time

nb_iterations = int(args[0])
result = []

for i in xrange(1, nb_iterations + 1):
    progress_file = variables.get(SchedulerVars.PA_TASK_PROGRESS_FILE.toString())
    ProgressFile.setProgress(progress_file,  i * (100 / nb_iterations))
    time.sleep(1)
    result.append(ProgressFile.getProgress(progress_file))
