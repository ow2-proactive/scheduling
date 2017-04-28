expectedHome = args[0]
expectedFile = new java.io.File(expectedHome).getCanonicalFile();
prop = variables.get("PA_SCHEDULER_HOME");
if (!expectedFile.equals(new java.io.File(prop).getCanonicalFile())) {
    throw new Error("Unexpected proactive.home value, expected " + expectedHome +
        " received " + prop);
}
result = true;
