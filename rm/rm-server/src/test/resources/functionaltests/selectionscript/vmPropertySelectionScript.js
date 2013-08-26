propertyName=args[0];
propertyValue=args[1];
print("selection script : check vm property name : "+ propertyName+" value : "+ propertyValue+"\n");
vmPropValue = java.lang.System.getProperty(propertyName);

if(propertyValue.equals(vmPropValue)) {
	print("VM property correctly setted\n");
	selected="true";
}
else {
	print("VM property NOT correctly setted\n");
	selected="false";
}
