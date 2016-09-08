import org.ow2.proactive.scheduler.common.job.*
import org.ow2.proactive.scheduler.common.task.*
import org.ow2.proactive.scheduler.common.task.dataspaces.InputAccessMode
import org.ow2.proactive.scheduler.common.task.dataspaces.OutputAccessMode
import org.ow2.proactive.scripting.*

try {
    job = new TaskFlowJob()
    job.setName("HelloJob")
    task = new ScriptTask()
    task.setName("HelloTask")
    inFile = new File("inFile.txt");
    inFile.write("SchedulerNodeClientTask")
    task.addInputFiles("inFile.txt", InputAccessMode.TransferFromUserSpace)
    task.addOutputFiles("outFile.txt", OutputAccessMode.TransferToUserSpace)
    task.setScript(new TaskScript(new SimpleScript("outFile = new File(\"outFile.txt\"); outFile.write(\"Hello \" + (new File(\"inFile.txt\").text) + \" I'm HelloTask\")", "groovy")))
    job.addTask(task)
    schedulerapi.connect()
    schedulerapi.pushFile("USERSPACE", "/", "inFile.txt", localspace + "/inFile.txt")
    jobid = schedulerapi.submit(job)
    println("job submitted");
    taskResult = schedulerapi.waitForTask(jobid.toString(), "HelloTask", 120000)
    schedulerapi.pullFile("USERSPACE", "/outFile.txt", localspace + "/outFile.txt")
    result = new File("outFile.txt").text
    println result

} catch (Exception e) {
    e.printStackTrace()
}
