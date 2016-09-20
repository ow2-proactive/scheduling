REMOTE_DIR = "testDataSpaceNodeClientPushDelete"

userspaceapi.connect()
globalspaceapi.connect()
inFile = new File("inFile.txt");
inFile.write("HelloWorld")

// push file
userspaceapi.pushFile(inFile, REMOTE_DIR + "/inFile.txt")
remotefiles = userspaceapi.listFiles(REMOTE_DIR, "*")
if (remotefiles.size() != 1) throw new IllegalStateException("expected one file found")
if (remotefiles.get(0) != "inFile.txt") throw new IllegalStateException("expected to find inFile.txt")

// delete file
userspaceapi.deleteFile(REMOTE_DIR + "/inFile.txt")
remotefiles = userspaceapi.listFiles(REMOTE_DIR, "*")
if (remotefiles.size() != 0) throw new IllegalStateException("expected zero file found")
result = "OK"


