/**
 * In the ProActive MapReduce framework we are forced to put the output files in
 * a sub-folder of the folder specified by the user. We mean if the user
 * specifies "output" as the name of the folder, in the output space of the job,
 * in which he wants to store the output files of the ReducerPATask tasks then
 * those files will be stored in a folder whose name will follow the format:
 * "output" + File.separator + "_temporary" + File.separator + "_attempt_*" +
 * File.separator + "part-r-*". This is do to the fact that we cannot change the
 * Hadoop code to force Hadoop to write the reducer output in the folder defined
 * by the user. Hence in this script we must select all the files whose names
 * match the "part-r-*" and we must rename them in something more simple (e.g.,
 * reducer_&lt;reducerId&gt;.out), put those file in the directory "output" and
 * remove all the other sub-folders of the folder "output" (i.e., remove the
 * sub-folders whose names match the string "_temporary" + File.separator +
 * "_attempt_*") To do this we must notice that the Script Environment has some
 * DataSpacesFileObjects bound to itself. Those DataSpacesFileObject are
 * accessible through the variables: - input: bound the INPUT data space of the
 * job - output: bound to the OUTPUT data space of the job - globalspace: bound
 * to the GLOBAL data space of the job - localspace: bound to the local data
 * space of the task As a consequence of all the previous explanation the
 * argument this script needs to execute are three: - the debug level - the
 * string to use to select the output files (using the Hadoop OutputFormat, this
 * means the script need in input a string like "output" + File.separator +
 * "_temporary" + File.separator + "_attempt_*" + File.separator + "part-r-*") -
 * the string that represents the prefix to use to build the name of the moved
 * files (i.e., something like "reducer_" that will substitute "part-r-" in the
 * original file name) We must notice that to retrieve the various strings to
 * use in this script (e.g., the string to use as the value of the reducerId) we
 * must split the string this script receives to select the ReducerPATask output
 * files conveniently (e.g., to retrieve the string that represent the reducerId
 * we split on the character "-").
 */

/*
 * we must notice that the function "importPackage" must be called with a
 * package, so we cannot use something like "java.util.List"
 */
importPackage(java.io);
importPackage(java.lang);
importPackage(java.util);
importPackage(org.objectweb.proactive.extensions.dataspaces.vfs.selector.fast);
importPackage(org.objectweb.proactive.extensions.dataspaces.api);

/*
 * Retrieve the property representing if the debug level logging is enabled or
 * not. WARNING: we must use the package notation ("java.lang.Boolean") because
 * the "Boolean" is also an object in the "javascript" language and if we do not
 * use the package notation then the "javascript" engine, e.g. Rhino, select the
 * javascript Boolean object and try to find the "parseBoolean" function that
 * that object does not define.
 */
var debugLogLevelPropertyName = args[0];
var debugLogLevel = java.lang.Boolean.parseBoolean(debugLogLevelPropertyName);
info("The value of the debug argument passed to the 'reducerPATaskOutputFileTransfer' post script is "
		+ debugLogLevelPropertyName);

// retrieve the string to use to select the ReducerPATask output files
var outputFileSelectionString = args[1];
debug(
		"The string that must be used to select the output files produced using the Hadoop OutputFormat is: "
				+ outputFileSelectionString, debugLogLevel);

/*
 * retrieve the string to use to build the name of the "moved" ReducerPATask
 * output file
 */
var movedFilePrefix = args[2];
debug(
		"The string that represents the prefix to use to build the name of the moved files is: "
				+ movedFilePrefix, debugLogLevel);

// String[] parts = ... of a string as "$OUTPUT/_temporary/_attempt_*/part-r-*"
var parts = outputFileSelectionString.split(File.separator);
var userDefinedOutputFolder = parts[0]; // the user defined name of the output
										// directory
var temporaryFolderToDelete = parts[1]; // the "_temporary" string
var originaryFileNamePrefix = parts[3].substring(0, parts[3].lastIndexOf("-"));

/*
 * control the order of the retrieved files: depthwise=true means that the
 * parent is added after its descendants
 */
var depthwise = true;

/*
 * List<DataSpacesFileObject> selectedDataSpacesFileObjectList = new ArrayList<DataSpacesFileObject>()
 */
var selectedDataSpacesFileObjectList = new ArrayList();

// create the FastFileSelector
var fastFileSelector = new FastFileSelector();

/*
 * We must notice that While creating a Java object is the same as in Java, to
 * create Java arrays in JavaScript we need to use Java reflection explicitly.
 * But once created the element access or length access is the same as in Java
 */
var includeFileArray = java.lang.reflect.Array.newInstance(java.lang.String, 1);
includeFileArray[0] = outputFileSelectionString;
fastFileSelector.setIncludes(includeFileArray);

/*
 * find the desired files in the OUTPUT data space. In the following code we
 * must notice we use the "output" variable to access the output data space
 */
FastSelector.findFiles(output, fastFileSelector, depthwise,
		selectedDataSpacesFileObjectList);
var selectedDataSpacesFileObjectListSize = selectedDataSpacesFileObjectList
		.size();
if (selectedDataSpacesFileObjectListSize > 0) {
	debug("The number of selected files in the data space whose real URI is "
			+ output.getRealURI() + " is "
			+ selectedDataSpacesFileObjectListSize, debugLogLevel);
	var currentParts = null;
	var currentName = null;
	var currentReducerId = null;
	var currentDataSpacesFileObjectName = null;
	var currentDataSpacesFileObject = null;
	var movedDataSpacesFileObject = null;
	for ( var i = 0; i < selectedDataSpacesFileObjectListSize; i++) {
		currentDataSpacesFileObject = selectedDataSpacesFileObjectList.get(i);
		currentDataSpacesFileObjectName = currentDataSpacesFileObject
				.getVirtualURI();
		debug(
				"The virtual URI of the currentDataSpacesFileObject at iteration '"
						+ i + "' is " + currentDataSpacesFileObjectName,
				debugLogLevel);
		/*
		 * we suppose the file name is "part-r-<reducerId>" so we retrieve the
		 * reducerId
		 */
		currentReducerId = currentDataSpacesFileObjectName.substring(
				currentDataSpacesFileObjectName.lastIndexOf("-") + 1,
				currentDataSpacesFileObjectName.length());
		debug("The currentReducerId at iteration '" + i + "' is "
				+ currentReducerId, debugLogLevel);
		currentName = userDefinedOutputFolder + File.separator
				+ movedFilePrefix + currentReducerId;
		debug(
				"The name of the file in which the originary one must be moved at iteration '"
						+ i + "' is " + currentName, debugLogLevel);
		movedDataSpacesFileObject = output.resolveFile(currentName);
		currentDataSpacesFileObject.moveTo(movedDataSpacesFileObject);
		/*
		 * TODO in the case we do not want to delete the whole "_temporary" we
		 * can delete each file after we moved it
		 * this will not work "currentDataSpacesFileObject.delete()"
		 * currentDataSpacesFileObject['delete']();
		 */
	}
	/*
	 * TODO the following method will delete "roughly" the whole directory
	 * "_temporary" (maybe we want to select the files of that directory to
	 *  delete)
	 */
	var temporayFolder = output.resolveFile(userDefinedOutputFolder
			+ File.separator + temporaryFolderToDelete);
	temporayFolder["delete"](FileSelector.SELECT_ALL);
	debug("The virtual URI of the temporary folder to delete is "
			+ temporayFolder.getVirtualURI(), debugLogLevel);
} else {
	debug("The string " + outputFileSelectionString
			+ " did select any file in the data space whose virtual URI is "
			+ output.getVirtualURI() + " while the real URI is "
			+ output.getRealURI(), debugLogLevel);
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