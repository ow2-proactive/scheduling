if ($IT == 0 && $REP == 0) {
	if (args.length < 1) {
		print("Requires a directory as first argument");
	} else {
		var f = new java.io.File(args[0] + "/output");
		f["delete"]();

		f = new java.io.File(args[0] + "/line");
		f["delete"]();
	}
}
