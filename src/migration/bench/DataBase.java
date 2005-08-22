package migration.bench;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;


public class DataBase extends Object {
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

    public static SearchResult searchLocalFile(String fileName, String keyWord) {
        File theInputFile = findFile(fileName);
        SearchResult theBigResult = new SearchResult();

        // Aborts if no input file
        if (theInputFile == null) {
            System.err.println("No file to search, search canceled");
            return theBigResult;
        }

        // Gets an input stream
        FileReader fr;
        BufferedReader br;

        try {
            fr = new FileReader(theInputFile);
            br = new BufferedReader(fr);
            theBigResult = performSearch(br, keyWord);
        } catch (IOException e) {
            System.err.println(e);
        }
        return theBigResult;
    }

    public static SearchResult searchInString(String theString, String keyWord) {
        SearchResult theBigResult = new SearchResult();

        // Aborts if no input file
        if (theString == null) {
            System.err.println("No string to search, search canceled");
            return theBigResult;
        }

        // Gets an input stream
        StringReader sr;
        BufferedReader br;

        sr = new StringReader(theString);
        br = new BufferedReader(sr);
        theBigResult = performSearch(br, keyWord);

        return theBigResult;
    }

    public static SearchResult performSearch(BufferedReader br, String keyWord) {
        SearchResult theBigResult = new SearchResult();
        String s;
        try {
            do {
                s = br.readLine();
                if (s != null) {
                    if ((s.indexOf(keyWord)) != -1) {
                        theBigResult.addLine(s);
                    }
                }
            } while (s != null);
        } catch (IOException e) {
            System.err.println(e);
        }

        return theBigResult;
    }
}
