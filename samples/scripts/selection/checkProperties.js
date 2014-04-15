/******************************************************
 * EXEMPLE SCRIPT : Using Property file
 * - Check 2 conditions with "Logical AND"
 *****************************************************/

/* We set our 2 conditions by creating "Condition" objects */
var condition1 = new org.ow2.proactive.scripting.helper.selection.Condition("ram",org.ow2.proactive.scripting.helper.selection.SelectionUtils.GREATER_THAN,"1024");
var condition2 = new org.ow2.proactive.scripting.helper.selection.Condition("architecture",org.ow2.proactive.scripting.helper.selection.SelectionUtils.CONTAINS, "6");

/* We set a table of "Condition" objects */
var conditions = new org.ow2.proactive.scripting.helper.selection.Conditions();

/* and put our 2 objects */
conditions.add(condition1);
conditions.add(condition2);

/* Evaluation by calling CheckConfig method */
if(org.ow2.proactive.scripting.helper.selection.SelectionUtils.checkProperties("samplePropertiesFile.txt",conditions)) {
    print("JS>selected = true");
    selected = true;
} else {
    print("JS>selected = false");
    selected = false;
}
