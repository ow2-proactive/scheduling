strClusterName = "localhost" 'Change to name of cluster if not run locally.
Set ArgsNamed = WScript.Arguments.Named
intTaskCount = ArgsNamed("tasks") 'Number of tasks to create (number of processors)
strCommandLine = ArgsNamed("application") 'command line of all tasks
strClassPath = ArgsNamed("classpath")   
strStdout = ArgsNamed("stdout")
strStderr = ArgsNamed("stderr")
strRuntime = ArgsNamed("runtime")
strUsername = "" 
strPassword = "" 
blnIsConsole = True 'True = cmd-line, False = GUI
intHandle = 0
Set objComputeCluster = CreateObject("Microsoft.ComputeCluster.Cluster")
objComputeCluster.Connect(strClusterName)
WScript.Echo "Compute Cluster Name: " & objComputeCluster.Name
Set objJob = objComputeCluster.CreateJob
objJob.MinimumNumberOfProcessors = intTaskCount
objJob.MaximumNumberOfProcessors = intTaskCount
if ArgsNamed.exists("runtime") Then 
	objJob.Runtime=strRuntime
end if
intJobID = objComputeCluster.AddJob((objJob))
WScript.Echo "Job ID: " & intJobID
For i = 1 To intTaskCount
  Set objTask = objComputeCluster.CreateTask
  objTask.CommandLine = strCommandLine
  objTask.SetEnvironmentVariable "CLASSPATH", strClassPath
  objTask.MinimumNumberOfProcessors = 1
  objTask.MaximumNumberOfProcessors = 1
  if argsNamed.exists("stdout") Then objTask.Stdout=strStdout & i & ".txt" end if
  if argsNamed.exists("stderr") Then objTask.Stderr=strStderr & i & ".txt" end if
  intTaskID = objComputeCluster.AddTask(objJob.ID, (objTask))
  WScript.Echo "Task ID: " & objTask.ID
Next
objComputeCluster.SubmitJob intJobID, strUsername, strPassword, _
 blnIsConsole, intHandle
WScript.Echo "Submitted Job " & intJobID
