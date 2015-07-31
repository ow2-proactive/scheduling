// Check if enough memory is available on the system

// expected memory size in Kb
var expectedMemorySize = 0;

var osb = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
var mem = java.lang.Math.round(osb.getFreePhysicalMemorySize() / 1024);

selected = java.lang.Integer.parseInt(mem) >= java.lang.Integer.parseInt(expectedMemorySize);

