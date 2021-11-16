import org.ow2.proactive.scheduler.common.job.JobVariable

List <JobVariable> variables = new java.util.ArrayList<JobVariable>()

variables.add(new JobVariable("name", "15", "PA:Integer"))
variables.add(new JobVariable("second", '${name}', "PA:Integer"))

signalapi.readyForSignal("my_signal", variables)

// Wait until one signal among those specified is received
outputParameters = signalapi.waitFor("my_signal")

println "Output parameters = " + outputParameters