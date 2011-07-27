// In case of RunAsMe, this script must be executed inside the node JVM before the creation of the forked JVM
// The java.io.tmpdir of the forked jvm is <NODE_TMPDIR || SCRATCH_DIR>

importClass(java.lang.System);
importClass(java.io.File);
importClass(org.ow2.proactive.scheduler.task.launcher.TaskLauncher);

var nodeTmpDir = System.getProperty("java.io.tmpdir");	
var tmpDir = System.getProperty(TaskLauncher.NODE_DATASPACE_SCRATCHDIR);
if (tmpDir == null) {
  tmpDir = System.getProperty("java.io.tmpdir"); 
}

// Set the fork environment java.io.tmpdir to the formed dir 
forkEnvironment.addJVMArgument("-Djava.io.tmpdir=" + tmpDir);