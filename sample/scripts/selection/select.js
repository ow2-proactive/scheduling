/******************************************************
 * EXEMPLE SCRIPT : Using a selection utils method to select a host
 *****************************************************/

importClass(org.ow2.proactive.scripting.helper.selection.SelectionUtils);

//Check if OS name is Windows and their is wireless connection available
if (SelectionUtils.checkOSName("windows") && SelectionUtils.checkWifi()){
	selected = true;
} else {
	selected = false;
}
