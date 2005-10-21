package trywithcatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;


public class Catcher {
    private String filename;
    private List parseData;
    private InputStream input;
    private OutputStream output;
    private int offsetDelta = 0;
    private int inputOffset = 0;
    public static final String INDENT = "    ";

    public Catcher(String f, OutputStream out, List p) {
        filename = f;
        parseData = p;
        output = out;
    }

    public void work() {
        File inputFile = new File(filename);
        File outputFile;
        try {
            input = new FileInputStream(inputFile);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        }

        try {
            Iterator iter = parseData.iterator();
            while (iter.hasNext()) {
                Anything a = (Anything) iter.next();
                a.work(this);
            }

            completeFile();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void catchUpFile(int newInputOffset) throws IOException {
        if (newInputOffset <= inputOffset) {
            return;
        }

        int delta = newInputOffset - inputOffset;
        byte[] data = new byte[delta];
        if (input.read(data) != delta) {
            throw new IOException();
        }
        inputOffset = newInputOffset;

        output.write(data);
    }

    public void addAtOffset(int offset, String str) throws IOException {
        catchUpFile(offset);

        byte[] data = str.getBytes();
        output.write(data);
        offsetDelta += data.length;
    }

    private void completeFile() throws IOException {
        byte[] data = new byte[4096];
        int len;
        while ((len = input.read(data)) > 0) {
            output.write(data, 0, len);
        }
    }

    public static String getNewline(Terminal align) {
        int indentLevel = align.getColumn();
        String str = "\n";
        while (indentLevel > 0) {
            str += " ";
            indentLevel--;
        }

        return str;
    }

    private void removeUntil(char b) throws IOException {
        int r;
        do {
            r = input.read();
            if (r == -1) {
                throw new IOException();
            }

            inputOffset++;
            offsetDelta--;
        } while (r != b);
    }

    public void removeCallAtOffset(int offset) throws IOException {
        catchUpFile(offset);

        removeUntil(';');
        removeUntil('\n');
    }
}
