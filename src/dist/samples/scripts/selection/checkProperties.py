# ******************************************************
# * EXEMPLE SCRIPT : Using Property file
# * - Check 2 conditions with "Logical AND"
# *****************************************************/
from org.ow2.proactive.scripting.helper.selection import SelectionUtils
from org.ow2.proactive.scripting.helper.selection import Condition
from org.ow2.proactive.scripting.helper.selection import Conditions

#--We set our 2 conditions by creating "Condition" objects
condition1 = Condition("ram", SelectionUtils.GREATER_THAN, "1024");
condition2 = Condition("architecture", SelectionUtils.CONTAINS, "6");

#--We set a table of "Condition" objects
conditions = Conditions();

#--and put our 2 objects
conditions.add(condition1);
conditions.add(condition2);

#--Evaluation by calling CheckConfig method
if SelectionUtils.checkProperties("samplePropertiesFile.txt",conditions):
    print "PY>selected = true";
    selected = True;
else:
    print "PY>selected = false";
	selected = False;
