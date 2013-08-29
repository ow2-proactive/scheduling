/**
 * this JavaScript implements the replication of the ReducerPATask. It is
 * specified in the "flowControl" tag of the MapperJoinPATask and it executes
 * just after the task's executable terminates. If the executable is a
 * JavaExecutable and return a result, the variable "result" will be set in the
 * script's environment. The script has to define the "enabled" boolean
 * variable; if it is set to other than true, the control flow action will never
 * be performed. The number of "replicas" of the MapperPATAsk to run is defined
 * by the variable "runs".
 *
 * This script takes three arguments as input:
 * <ul>
 * <li>the String representing the name of the variable to use to retrieve the
 * id of the task this script is attached to Often the String is the same as
 * "org.ow2.proactive.scheduler.task.launcher.SchedulerVars.JAVAENV_TASK_ID_VARNAME.toString()";</li>
 * <li>the String representation of a boolean value indicating if the logger
 * has to print debug messages or not;</li>
 * <li>the number of ReducerPATask tasks to execute;</li>
 * </ul>
 */

importPackage(java.io);
importPackage(java.lang);

// Retrieve the "pas.task.id"
var taskIdPropertyName = args[0];
var taskId = System.getProperty(taskIdPropertyName);

/*
 * Retrieve the property representing if the debug level logging is enabled or
 * not. WARNING: we must use the package notation ("java.lang.Boolean") because
 * the "Boolean" is also an object in the "javascript" language and if we do not
 * use the package notation then the "javascript" engine, e.g. Rhino, select the
 * javascript Boolean object and try to find the "parseBoolean" function that
 * that object does not define.
 */
var debugLogLevelPropertyName = args[1];
var debugLogLevel = java.lang.Boolean.parseBoolean(debugLogLevelPropertyName);

var numberOfReducerPATaskString = args[2];
debug(
		"The string that represents the number of ReducerPATask tasks is (args[2]): "
				+ numberOfReducerPATaskString, debugLogLevel);

// print the information about the "pas.task.id"
debug(taskIdPropertyName + " is " + taskId, debugLogLevel);

if ((numberOfReducerPATaskString != null)
		&& (numberOfReducerPATaskString != "")) {
	// note that we have to transform the 'numberOfReducerPATaskString' variable
	// explicitly into an Integer, otherwise we will get an exception
	runs = Integer.parseInt(numberOfReducerPATaskString);

	debug("The number of ReducerPATask to execute is: " + runs, debugLogLevel);

	if (runs > 0) {
		// this means at least a ReducerPATask has to be run
		enabled = true;
	}
} else {
	enabled = false;
}

/**
 * function to print an info message on the taskflow job log file
 *
 * @param message
 *            the String to print
 * @return void
 */
function info(message) {
	message = "[INFO] " + message;
	println(message);
}

/**
 * function to print a debug message on the taskflow job log file
 *
 * @param message
 *            the String to print
 * @param debugLogLevel
 *            the boolean representing if the debug messages will be printed or
 *            not
 * @return void
 */
function debug(message, debugLogLevel) {
	if (debugLogLevel) {
		message = "[DEBUG] " + message;
		println(message);
	}
}
