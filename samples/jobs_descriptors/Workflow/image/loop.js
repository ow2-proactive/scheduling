importPackage(java.io);

if (args.length < 1) {
	print("Usage:   ./dloop.js directory");
} else {
	var dirIn = new File(args[0] + "/in/");
	var dirOut = new File(args[0] + "/out/");

	var listIn = dirIn.listFiles();
	var listOut = dirOut.listFiles();

	loop = (listIn.length != listOut.length);
}
