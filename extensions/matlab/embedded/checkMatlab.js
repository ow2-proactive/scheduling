importPackage(java.lang);
if (System.getProperty("os.name").startsWith("Windows")) {
        selected=false;
}
else {       
selected = (java.lang.Runtime.getRuntime().exec("which matlab2007b").waitFor() == 0);
if (!selected) selected=(java.lang.Runtime.getRuntime().exec("which matlab2007a").waitFor() == 0);
if (!selected) selected=(java.lang.Runtime.getRuntime().exec("which matlab2006b").waitFor() == 0);
if (!selected) selected=(java.lang.Runtime.getRuntime().exec("which matlab2006a").waitFor() == 0);
if (!selected) selected=(java.lang.Runtime.getRuntime().exec("which matlab71").waitFor() == 0);
if (!selected) selected=(java.lang.Runtime.getRuntime().exec("which matlab7").waitFor() == 0);
if (selected) print("Good : "+ java.net.InetAddress.getLocalHost().getHostName()+"\n");
else print("Not Good : "+ java.net.InetAddress.getLocalHost().getHostName()+"\n");
}
