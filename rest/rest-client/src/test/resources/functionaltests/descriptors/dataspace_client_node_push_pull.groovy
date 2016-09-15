import com.google.common.io.Files


REMOTE_DIR = "testDataSpaceNodeClientPushPull"

userspaceapi.connect()

// push file
inFile = new File("inFile.txt");
inFile.write("HelloWorld")
userspaceapi.pushFile(inFile, REMOTE_DIR + "/inFile.txt")
remotefiles = userspaceapi.listFiles(REMOTE_DIR, "*")
if (remotefiles.size() != 1) throw new IllegalStateException("expected one file found")
if (remotefiles.get(0) != "inFile.txt") throw new IllegalStateException("expected to find inFile.txt")

// pull file
outFile = new File("outFile.txt")
userspaceapi.pullFile(REMOTE_DIR + "/inFile.txt", outFile)
if (!Files.equal(inFile, outFile)) throw new IllegalStateException("Donwloaded file should be the same as the original")
result = outFile.text


