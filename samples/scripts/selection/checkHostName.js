importPackage(java.lang);
importPackage(java.io);
importPackage(java.util);
importPackage(java.exception);
importClass(org.ow2.proactive.scripting.helper.selection.SelectionUtils);

// Get the arguments of the script:
// args[0] : the hostname to select
var hostName = args[0];
   
/* Check if the name of the host that holds the node is equal to the name given in argument */
if (SelectionUtils.checkHostName(hostName)) 
{
	selected = true;
	println("Hostname = " + hostName + " ==> selected");
    }
else 
    {
	selected = false;
	println("Hostname <> " + hostName + " ==> not selected");
    }

