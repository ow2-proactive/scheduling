importPackage(java.io);

if (args.length < 1) {
	print("Usage:   ./dup.js directory");
} else {
	var dir = new File(args[0] + "/tmp.in/" + $IT);
	runs = dir.listFiles().length;
}
