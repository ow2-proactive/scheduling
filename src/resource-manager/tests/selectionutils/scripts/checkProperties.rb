require 'java'
SelectionUtils = Java::org.ow2.proactive.scripting.helper.selection.SelectionUtils
Condition = Java::org.ow2.proactive.scripting.helper.selection.Condition

#--We set our 2 conditions by creating "Condition" objects
condition1 = Condition.new("ram", SelectionUtils::GREATER_THAN, "1024")
condition2 = Condition.new("architecture", SelectionUtils::MATCH, "6")

#--We set a table of "Condition" objects
conditions = Array.new

#--and put our 2 objects
conditions.push(condition1)
conditions.push(condition2)

#--Converting RubyArray to JavaArray
#conditions = conditions.to_java(Condition)


if SelectionUtils.checkProperties($propertiesFile,conditions)
    $selected = true;
else
    $selected = false;
end
