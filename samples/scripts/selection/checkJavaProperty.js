var propertyName = "java.vendor";
var expectedValue = "Oracle Corporation";

/* Check if java property 'propertyName' has value 'expectedValue' */
if (org.ow2.proactive.scripting.helper.selection.SelectionUtils.checkJavaProperty(propertyName, expectedValue)) {
    selected = true;
} else {
    selected = false;
}

