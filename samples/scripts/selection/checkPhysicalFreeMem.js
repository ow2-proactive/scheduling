// Check if enough memory is available on the system
// args[0] : expected memory size in Kb

// WARNING : works only with Sun JVMs > 1.5

var selected = false;

var osb = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
var mem = osb.getFreePhysicalMemorySize()/1024;
print(mem+" Kb available");
//var swap = osb.getFreeSwapSpaceSize()/1024;
//print(swap+" Kb");
selected=java.lang.Integer.parseInt(mem)>java.lang.Integer.parseInt(args[0]);
print("FreeMem check : "+selected);