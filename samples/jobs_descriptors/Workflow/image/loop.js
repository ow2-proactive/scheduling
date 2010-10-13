importPackage(java.io);

if (args.length < 1) {
	print("Usage:   ./dloop.js directory");
} else {
	var dirIn = new File(args[0] + "/in/");
	var dirOut = new File(args[0] + "/out/");

	var listIn = dirIn.listFiles();
	var listInLen = listIn.length;
	for (var i=0; i < listIn.length; i++) {
		if (listIn[i].getName().substring(0, 1).equals(".")) {
			listInLen--;
		}
	}

	var listOut = dirOut.listFiles();
	var listOutLen = listOut.length;
	for (var i=0; i < listOut.length; i++) {
		if (listOut[i].getName().substring(0, 1).equals(".")) {
			listOutLen--;
		}
	}

	loop = (listInLen != listOutLen);
}
