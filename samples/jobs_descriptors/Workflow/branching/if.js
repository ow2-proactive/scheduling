var f = new java.io.File(args[0] + "/line");

if (f.exists()) {
	var input = new java.io.BufferedReader(new FileReader(f));
	var len = input.readLine().length();
	input.close();

	if (len < 40) {
		branch = "if";
	} else {
		branch = "else";
	}
}
