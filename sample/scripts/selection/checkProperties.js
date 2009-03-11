/******************************************************
 * EXEMPLE SCRIPT : Using Property file
 * - Check 2 conditions with "Logical AND"
 *****************************************************/

importClass(org.ow2.proactive.scripting.helper.selection.Condition);
importClass(org.ow2.proactive.scripting.helper.selection.SelectionUtils);

//--We set our 2 conditions by creating "Condition" objects
var condition1 = new Condition("ram",SelectionUtils.GREATER_THAN,"1024");
var condition2 = new Condition("architecture",SelectionUtils.MATCH, ".*6$");

//--We set a table of "Condition" objects
var conditions = new Array();

//--and put our 2 objects
conditions[0] = condition1;
conditions[1] = condition2;

//--Evaluation by calling CheckConfig method
if(SelectionUtils.checkProperties("samplePropertiesFile.txt",conditions)) {
    println("JS>selected = true");
    selected = true;
} else {
    println("JS>selected = false");
    selected = false;
}
