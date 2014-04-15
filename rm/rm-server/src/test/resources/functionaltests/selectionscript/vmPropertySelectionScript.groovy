def propertyName=args[0];
def propertyValue=args[1];
println "selection script : check vm property name : "+ propertyName+" value : "+ propertyValue
vmPropValue = System.getProperty(propertyName);

if(propertyValue.equals(vmPropValue)) {
	println "VM property correctly set"
	selected="true";
}
else {
	println "VM property NOT correctly set"
	selected="false";
}
