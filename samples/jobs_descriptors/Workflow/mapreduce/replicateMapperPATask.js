/**
 * This JavaScript performs the replication of the MapperPATask task.
 *
 * It is specified in the "controlFlow" tag of the SplitterPATask and it executes just after
 * the task's executable terminates. If the executable is a JavaExecutable and return a result,
 * the variable "result" will be set in the script's environment.
 * The script has to define the "enabled" boolean variable; if it is set to other than true, the
 * control flow action will never be performed.
 * The number of "replicas" of the MapperPATask to run is defined by the variable "runs".
 *
 * This script takes three arguments as input:
 * <ul>
 * <li>the String representing the name of the variable to use to retrieve the id of the task this script is attached to
 * 		Often the String is the same as "org.ow2.proactive.scheduler.task.launcher.SchedulerVars.JAVAENV_TASK_ID_VARNAME.toString()";</li>
 * <li>the String representation of a boolean value indicating if the logger has to print debug messages or not;</li>
 * <li>TODO delete this: the String to use as the name of the property to propagate the number of MapperPATask tasks to execute;</li>
 * </ul>
 */

importPackage(java.io);
importPackage(java.lang);
importPackage(org.ow2.proactive.scripting);
importPackage(org.ow2.proactive.scripting.helper.filetransfer);



//Retrieve the "pas.task.id"
var taskIdPropertyName = args[0];
var taskId = System.getProperty(taskIdPropertyName);

/*
 * Retrieve the property representing if the debug level logging is enabled or not.
 * WARNING: we must use the package notation ("java.lang.Boolean") because the "Boolean"
 * 	can also be the type of an object in the "javascript" language and if we do not use the package
 * 	notation then the "javascript" engine, e.g. Rhino, selects the javascript Boolean
 * 	object and try to find the "parseBoolean" function of that object. But such a function does not
 * 	exists in the "javascript" Boolean object.
 */
var debugLogLevelPropertyName = args[1];
var debugLogLevel = java.lang.Boolean.parseBoolean(debugLogLevelPropertyName);

/*
 * The following commented code is not needed anymore because in the previous implementation
 * we propagated the property representing the number of MapperPATask because we calculated
 * the number of ReducerPATask as a function of the number of MapperPATask. Now the number of
 * ReducerPATask to execute is set by the user as a property of the Hadoop MapReduce Job
 */
//var numberOfMapperPATaskPropertyName = args[2];
//debug("The name of the property that represents the number of MapperPATask tasks is (args[2]): " + numberOfMapperPATaskPropertyName, debugLogLevel);

// print the information about the "pas.task.id"
debug(taskIdPropertyName + " is " + taskId, debugLogLevel);

/*
 * We must notice that the result variable is the TaskResult.value() if it exists.
 * The SplitterPATask generates a list of InputSplit so the TaskResult.value is a List<InputSplit>.
 */
debug("Result size of the task is " + result.size(), debugLogLevel);


if((result != null) && (result.size() > 0)) {
	// This means at least an InputSplit was produced so we can perform the replication of the MapperPATask tasks
	enabled = true;
	debug("The Replicate action is enabled for MapperPATask tasks", debugLogLevel);

	runs = result.size();
	debug("The number of MapperPATask tasks to execute is: " + runs, debugLogLevel);

//	var properties = System.getProperties();
//	properties.setProperty("" + numberOfMapperPATaskPropertyName, "" + runs);
//	PropertyUtils.propagateProperty(numberOfMapperPATaskPropertyName);
}
else {
	enabled = false;
	debug("The Replicate action is not enabled for MapperPATask tasks", debugLogLevel);
}


/**
 * function to print an info message on the taskflow job log file
 * @param message the String to print
 * @return void
 */
function info(message) {
	message = "[INFO] " + message;
	println(message);
}

/**
 * function to print a debug message on the taskflow job log file
 * @param message the String to print
 * @param debugLogLevel the boolean representing if the debug messages will be printed or not
 * @return void
 */
function debug(message, debugLogLevel) {
	if(debugLogLevel) {
		message = "[DEBUG] " + message;
		println(message);
	}
}
