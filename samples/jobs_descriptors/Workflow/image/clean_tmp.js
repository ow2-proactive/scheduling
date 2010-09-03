importPackage(java.io);

if ($IT == 0 && $REP == 0) {
	if (args.length < 1) {
		print("Requires a directory as first argument");
	} else {
		var f = new File(args[0] + "/tmp.in/");
		_delete(f);

		f = new File(args[0] + "/tmp.out/");
		_delete(f);

		f = new File(args[0] + "/out/");
		_delete(f, false);
	}
}
function _delete(f) {
	_delete(f, true);
}

function _delete(f, root) {
	if (f.isDirectory()) {
		var list = f.listFiles();
		for (var i=0; i < list.length; i++) {
			_delete(list[i]);
			list[i]["delete"]();
		}
	}
	if (root) {
		f["delete"]();
	}
}
