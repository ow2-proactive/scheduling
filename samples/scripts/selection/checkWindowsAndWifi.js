/* Check if OS name is Windows and if wireless connection is available */
if (org.ow2.proactive.scripting.helper.selection.SelectionUtils.checkOSName("windows")
        && org.ow2.proactive.scripting.helper.selection.SelectionUtils.checkWifi()){
    selected = true;
} else {
    selected = false;
}
