/******************************************************
 * EXEMPLE SCRIPT : Using a selection utils method to select a host
 *****************************************************/

/* Check if OS name is Windows and their is wireless connection available */
if (org.ow2.proactive.scripting.helper.selection.SelectionUtils.checkOSName("windows") && SelectionUtils.checkWifi()){
	selected = true;
} else {
	selected = false;
}
