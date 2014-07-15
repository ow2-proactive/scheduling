
// Get the arguments of the script:
// args[0] : the file path that must exist on the target host
var filePath = args[0];
   
/* Check if the given file path exist or not. */
if (org.ow2.proactive.scripting.helper.selection.SelectionUtils.checkFileExist(filePath))
{
	selected = true;
	print(filePath + " exists ==> selected");
    }
else 
    {
	selected = false;
    print(filePath + " does not exist ==> not selected");
}

