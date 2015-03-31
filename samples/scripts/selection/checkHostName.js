// the hostname to select
var hostName = "oops";
   
/* Check if the name of the host that holds the node is equal to the name given in argument */
if (org.ow2.proactive.scripting.helper.selection.SelectionUtils.checkHostName(hostName)) {
    selected = true;
} else {
    selected = false;
}

