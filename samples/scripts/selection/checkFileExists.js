// the file path that must exist on the target host
// the path is relative to SCHEDULER_HOME
var filePath = "myfile.txt";
   
/* Check if the given file path exist or not. */
if (org.ow2.proactive.scripting.helper.selection.SelectionUtils.checkFileExist(filePath)) {
    selected = true;
} else {
    selected = false;
}

