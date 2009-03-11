#******************************************************
#* EXEMPLE SCRIPT : Using a selection utils method to select a host
#******************************************************
require 'java'
SelectionUtils = Java::org.ow2.proactive.scripting.helper.selection.SelectionUtils

#check if free memory is at least 1Go
if SelectionUtils.checkFreeMemory(1073741824)
	selected = true;
else
	selected = false;
end
