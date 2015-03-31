# Script using property file and two logical conditions

from org.ow2.proactive.scripting.helper.selection import SelectionUtils
from org.ow2.proactive.scripting.helper.selection import Condition
from org.ow2.proactive.scripting.helper.selection import Conditions

#-- We set our 2 conditions by creating "Condition" objects
condition1 = Condition("ram", SelectionUtils.GREATER_THAN, "1024");
condition2 = Condition("architecture", SelectionUtils.CONTAINS, "6");

#-- We create a list of "Condition" objects
conditions = Conditions();

#-- and insert our 2 objects
conditions.add(condition1);
conditions.add(condition2);

#-- Evaluation by calling CheckConfig method
if SelectionUtils.checkProperties("samples/scripts/selection/samplePropertiesFile.txt", conditions):
    selected = True;
else:
    selected = False;

