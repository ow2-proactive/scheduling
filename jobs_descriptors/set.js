print("Beginning of Pre-Script\n");
str = java.net.InetAddress.getLocalHost().getHostName();
java.lang.System.setProperty("user.property1", str);
print("Setting system property user.property1 = " + str + "\n");
print("End of Pre-Script\n");
