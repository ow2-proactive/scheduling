require 'java'

SelectionUtils = Java::org.ow2.proactive.scripting.helper.selection.SelectionUtils

#check if free memory is at least 1Go
if SelectionUtils.checkFreeMemory(expectedMemorySize)
    $selected = true;
else
    $selected = false;
end

