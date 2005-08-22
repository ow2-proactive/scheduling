package migration.bench;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.DeflaterOutputStream;

import org.objectweb.proactive.ProActive;


public class Converter {
    public void echo() {
        System.out.println("Ta mere");
    }

    public static File findFile(String fileName) {
        File theFile = new File(fileName);
        if (theFile.exists()) {
            if (theFile.canRead()) {
                System.out.println("File " + theFile.getAbsolutePath() +
                    " correctly found");
                return theFile;
            } else {
                System.err.println("Cannot read file " + fileName +
                    ", although file exists.");
                System.err.println("Hint: check file access rights.");
                return null;
            }
        } else {
            System.err.println("File " + fileName + " does not exist.");
            System.err.println("Hint: absolute path is " +
                theFile.getAbsolutePath() + ".");
            return null;
        }
    }

    public char[] getLocalFileWithoutCompression(String fileName) {
        StringBuffer total = new StringBuffer();
        String s;
        File theInputFile = findFile(fileName);

        // Aborts if no input file
        if (theInputFile == null) {
            System.err.println("No file to search, search canceled");
            return null;
        }

        // Gets an input stream
        FileReader fr;

        BufferedReader br;
        try {
            fr = new FileReader(theInputFile);
            br = new BufferedReader(fr);

            while ((s = br.readLine()) != null) {
                total.append(s); // = total + s;
                total.append("\n");
                //   System.out.println("Parsing new line " + ++i);
            }
        } catch (IOException e) {
            System.err.println(e);
        }

        //	System.out.println("Size of the bytes ByteArrayOutputStream = " + byteArrayOutputStream.toByteArray().length);
        return total.toString().toCharArray();
    }

    //We use an array of char because of bug 4026823, 4039553
    public byte[] getLocalFile(String fileName) {
        StringBuffer total = new StringBuffer();
        String s;
        File theInputFile = findFile(fileName);

        // Aborts if no input file
        if (theInputFile == null) {
            System.err.println("No file to search, search canceled");
            return null;
        }

        // Gets an input stream
        FileReader fr;
        BufferedReader br;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            fr = new FileReader(theInputFile);
            br = new BufferedReader(fr);

            while ((s = br.readLine()) != null) {
                total.append(s); // = total + s;
                total.append("\n");
                //   System.out.println("Parsing new line " + ++i);
            }

            byteArrayOutputStream = new ByteArrayOutputStream();
            DeflaterOutputStream deflater = new DeflaterOutputStream(byteArrayOutputStream);
            ObjectOutputStream objectOut = new ObjectOutputStream(deflater);
            objectOut.writeObject(total);
            objectOut.close();

            // 	System.out.println("Length of the StringBuffer total  = " + total.length() );
            // 			System.out.println("Length of the ByteArrayOutputStream byteArrayOutputStream  = " + byteArrayOutputStream.size() );
            //System.out.println("Compression ratio = " + total.length()/ byteArrayOutputStream.size());
        } catch (IOException e) {
            System.err.println(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println(
                "Usage: java migration.bench.Converter rmi://hostname/objectName");
            System.exit(-1);
        }
        try {
            //First we create a new active object
            Converter converter = (Converter) ProActive.newActive("migration.bench.Converter",
                    null);

            //Then we register
            ProActive.register(converter, args[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
