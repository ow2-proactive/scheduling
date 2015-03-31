from org.ow2.proactive.scripting.helper.selection import SelectionUtils

# Check if java property 'java.vendor' value is 'Oracle Corporation'
if SelectionUtils.checkJavaProperty("java.vendor", "Oracle Corporation"):
    selected = True;
else:
    selected = False;

