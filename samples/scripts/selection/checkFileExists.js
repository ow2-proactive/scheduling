importPackage(java.lang);
importPackage(java.io);
importPackage(java.util);
importPackage(java.exception);
importClass(org.ow2.proactive.scripting.helper.selection.SelectionUtils);

// Get the arguments of the script:
// args[0] : the file path that must exist on the target host
var filePath = args[0];
   
/* Check if the given file path exist or not. */
if (SelectionUtils.checkFileExist(filePath)) 
{
	selected = true;
	println(filePath + " exists ==> selected");
    }
else 
    {
	selected = false;
	println(filePath + " does not exist ==> not selected");
    }

