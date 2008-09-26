importPackage(java.lang);
if (System.getProperty("os.name").startsWith("Windows")) {
    selected = (java.lang.Runtime.getRuntime().exec("REG QUERY \"HKEY_LOCAL_MACHINE\\SOFTWARE\\Mathworks\\MATLAB\" /s").waitFor() == 0);
}
else {       
    selected = (java.lang.Runtime.getRuntime().exec("which matlab2007b").waitFor() == 0);
    if (!selected) selected=(java.lang.Runtime.getRuntime().exec("which matlab2007a").waitFor() == 0);
    if (!selected) selected=(java.lang.Runtime.getRuntime().exec("which matlab2006b").waitFor() == 0);
    if (!selected) selected=(java.lang.Runtime.getRuntime().exec("which matlab2006a").waitFor() == 0);
    if (!selected) selected=(java.lang.Runtime.getRuntime().exec("which matlab71").waitFor() == 0);
    if (!selected) selected=(java.lang.Runtime.getRuntime().exec("which matlab").waitFor() == 0);
}
if (selected) print("Good : "+ java.net.InetAddress.getLocalHost().getHostName()+"\n");
else print("Not Good : "+ java.net.InetAddress.getLocalHost().getHostName()+"\n");

