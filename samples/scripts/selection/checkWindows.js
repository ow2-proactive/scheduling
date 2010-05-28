importClass(org.ow2.proactive.scripting.helper.selection.SelectionUtils);

/* Check if OS name is Windows */
if (SelectionUtils.checkOSName("windows")){
	selected = true;
} else {
	selected = false;
}
