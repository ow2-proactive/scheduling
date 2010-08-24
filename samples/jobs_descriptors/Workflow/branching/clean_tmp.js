importPackage(java.io);

if ($IT == 0 && $DUP == 0) {
	if (args.length < 1) {
		print("Requires a directory as first argument");
	} else {
		var f = new File(args[0] + "/output");
		f["delete"]();

		f = new File(args[0] + "/line");
		f["delete"]();
	}
}
