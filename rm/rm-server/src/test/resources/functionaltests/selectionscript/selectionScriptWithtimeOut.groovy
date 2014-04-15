timeout = args[0] as int;
property = (args as List)[1]
if (property == null || System.getProperty(property) != null) {
    println "selection script that sleeps : " + timeout
    Thread.sleep(timeout);
    println "End of selection script"
}
selected = true;
