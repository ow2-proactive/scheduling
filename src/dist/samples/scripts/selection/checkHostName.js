// Get the arguments of the script:
// args[0] : the hostname to select
var hostName = args[0];
   
/* Check if the name of the host that holds the node is equal to the name given in argument */
if (org.ow2.proactive.scripting.helper.selection.SelectionUtils.checkHostName(hostName))
{
	selected = true;
	print("Hostname = " + hostName + " ==> selected");
    }
else 
    {
	selected = false;
	print("Hostname <> " + hostName + " ==> not selected");
    }

