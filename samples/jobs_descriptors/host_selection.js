exclusionString=args[0];
print("Verif : hostname "+ java.net.InetAddress.getLocalHost().getHostName()+" must not contain  : "+ exclusionString+"\n");
selected = !java.net.InetAddress.getLocalHost().getHostName().matches(".*"+exclusionString+".*");
