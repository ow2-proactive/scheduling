expectedHome = args[0]
expectedFile = new java.io.File(expectedHome).getCanonicalFile();
prop = java.lang.System.getProperty("proactive.home");
if (!expectedFile.equals(new java.io.File(prop).getCanonicalFile())) {
    throw new Error("Unexpected proactive.home value, expected " + expectedHome +
        " received " + prop);
}
prop = java.lang.System.getProperty("pa.rm.home");
if (!expectedFile.equals(new java.io.File(prop).getCanonicalFile())) {
    throw new Error("Unexpected pa.rm.home value, expected " + expectedHome +
        " received " + prop);
}
prop = java.lang.System.getProperty("pa.scheduler.home");
if (!expectedFile.equals(new java.io.File(prop).getCanonicalFile())) {
    throw new Error("Unexpected pa.scheduler.home value, expected " + expectedHome +
        " received " + prop);
}
result = true;
