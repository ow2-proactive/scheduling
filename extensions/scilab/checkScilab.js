importPackage(java.lang);

if (System.getProperty("os.name").startsWith("Windows")) {
    try {
        selected = (java.lang.Runtime.getRuntime().exec("REG QUERY \"HKEY_LOCAL_MACHINE\\SOFTWARE\\Scilab\" /s").waitFor() == 0);
    }
    catch(err) {
        selected = false;
    }
}
else {
    try {
        selected = (java.lang.Runtime.getRuntime().exec("which scilab").waitFor() == 0);        
    }
    catch(err) {
        selected = false;
    }
}

if (selected) print("Good : "+ java.net.InetAddress.getLocalHost().getHostName()+"\n");
else print("Not Good : "+ java.net.InetAddress.getLocalHost().getHostName()+"\n");

