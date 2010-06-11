importPackage(java.lang);

selected = false;

if (System.getProperty("os.name").startsWith("Windows")) {
    try {
        selected = (java.lang.Runtime.getRuntime().exec("REG QUERY \"HKEY_LOCAL_MACHINE\\SOFTWARE\\Mathworks\\MATLAB\" /s").waitFor() == 0);
    }
    catch(err) {
        selected = false;
    }
}
else {
    try {
        myArray = [ 'matlab2009b', 'matlab2009a', 'matlab2008b', 'matlab2008a', 'matlab2007b', 'matlab2007a', 'matlab2006b', 'matlab2006a', 'matlab71' ];

        for (i=0; i < myArray.length && !selected ; i++) {
            selected = (java.lang.Runtime.getRuntime().exec("which "+myArray[i]).waitFor() == 0);
        }
    }
    catch(err) {
        selected = false;
    }
}

if (selected) print("Good : "+ java.net.InetAddress.getLocalHost().getHostName()+"\n");
else print("Not Good : "+ java.net.InetAddress.getLocalHost().getHostName()+"\n");







