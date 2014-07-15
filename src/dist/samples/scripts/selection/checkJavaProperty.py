#******************************************************
#* EXEMPLE SCRIPT : Using a selection utils method to select a host
#*****************************************************/
from org.ow2.proactive.scripting.helper.selection import SelectionUtils

#Check if java property 'a.jvm.property' value is 'toto'
if SelectionUtils.checkJavaProperty("a.jvm.property", "toto"):
	selected = true;
else:
	selected = false;
