expectedHome = args[0]
expectedFile = new java.io.File(expectedHome).getCanonicalFile();
prop = variables.get("proactive.home");
if (!expectedFile.equals(new java.io.File(prop).getCanonicalFile())) {
    throw new Error("Unexpected proactive.home value, expected " + expectedHome +
        " received " + prop);
}
prop = variables.get("pa.rm.home");
if (!expectedFile.equals(new java.io.File(prop).getCanonicalFile())) {
    throw new Error("Unexpected pa.rm.home value, expected " + expectedHome +
        " received " + prop);
}
prop = variables.get("pa.scheduler.home");
if (!expectedFile.equals(new java.io.File(prop).getCanonicalFile())) {
    throw new Error("Unexpected pa.scheduler.home value, expected " + expectedHome +
        " received " + prop);
}
result = true;
