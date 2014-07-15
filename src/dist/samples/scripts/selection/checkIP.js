// Get the arguments of the script:
// args[0] : the IP mask of the host to be selected
var ipMask = args[0];

/* Check if the IP of the host that holds the node is equal to the IP given in argument 
The IP can be given as x.x.x.x or using the token * to match a network for example. (ie x.x.x.*)
 */

if (org.ow2.proactive.scripting.helper.selection.SelectionUtils.checkIp(ipMask))
{
	selected = true;
	print("Host's IP matches with " + args[0] + " ==> selected");
    }
else 
    {
	selected = false;
	print("Host's IP does not match with " + args[0] + " ==> not selected");
    }

