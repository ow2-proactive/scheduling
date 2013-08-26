timeout=args[0];
property=args[1];
if (property == null || java.lang.System.getProperty(property)!=null ) {
	print("selection script that sleeps : " + timeout+ "\n");
	java.lang.Thread.sleep(timeout);
	print("End of selection script\n");
}
selected = true ;
