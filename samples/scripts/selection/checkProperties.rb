# Script using property file and two logical conditions

require 'java'

SelectionUtils = Java::org.ow2.proactive.scripting.helper.selection.SelectionUtils
Condition = Java::org.ow2.proactive.scripting.helper.selection.Condition
Conditions = Java::org.ow2.proactive.scripting.helper.selection.Conditions

#-- We set our 2 conditions by creating "Condition" objects
condition1 = Condition.new("ram", SelectionUtils::GREATER_THAN, "1024")
condition2 = Condition.new("architecture", SelectionUtils::CONTAINS, "6")

#-- We create a list of "Condition" objects
conditions = Conditions.new

#-- and insert our 2 objects
conditions.add(condition1)
conditions.add(condition2)

if SelectionUtils.checkProperties("samples/scripts/selection/samplePropertiesFile.txt", conditions)
    $selected = true;
else
    $selected = false;
end

