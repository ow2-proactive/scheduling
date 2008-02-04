package doc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import java.io.InputStreamReader;
import java.util.Hashtable;


//TODO use log4j instead of sysout (I know, it's bad)
//TODO utterly bad code, check for input, do proper logging, make generic, handle exceptions properly
//TODO maybe rewrite with regular expressions ?
//TODO document properly
//TODO add generic file parsing
//TODO indent the extracted code
/**
 * @author vjuresch
 *
 */
public class Snippetizer {

    //TODO remove constant
    protected String startAnnotation = "//@snippet-start";
    protected String endAnnotation = "//@snippet-end";

    protected File targetDir;
    protected File rootDir;

    public Snippetizer(File root, File targetDir) {
        this.rootDir = root;
        this.targetDir = targetDir;
    }

    /**
     * Extracts the snippets of code from all the java files
     * in the directories and subdirectories
     * 
     * ** recursive method **
     */
    public void startExtraction(File dir) {

        //System.out.println("Entering dir " + dir);

        // List the source directory. If the file is a dir recurse,
        // if the file is a java file check for Extract annotations
        // otherwise ignore

        File[] elements = dir.listFiles();

        for (int i = 0; i < elements.length; ++i) {
            File file = elements[i];
            if (file.isDirectory()) {
                startExtraction(file);
                //TODO remove constant
            } else if (file.getName().endsWith(".java") && !file.getName().equals("Snippetizer.java")) {
                try {
                    if (fileIsValid(file))
                        extractSnippets(file);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } // fi
        } // rof
    }

    //quick (is it?) and dirty validator
    /**
     * Checks that for each start tag there is a corresponding
     * end tag and there are no stop tags before end
     * tags. Tags can be imbricated. 
     * 
     * 	Validity conditions
     * 1.  same number of start and end tags
     * 2.  start tags always have a higher index then corresponding end tags (are before)
     * 3.  no duplicate start or end tags (should be checked globally somehow)
     * 4.  an end tag has a corresponding start tag and vice versa 
     * 5.  check for empty tags
     * 
     * @param file to be checked
     * @return a boolean value saying if the file is valid or not
     */
    public boolean fileIsValid(File file) throws Exception {
        //conditions for validity
        //1.  same number of start and end tags
        //2.  start tags always have a higher index then corresponding end tags (are before)
        //3.  no duplicate start or end tags (should be checked globally somehow)
        //4.  an end tag has a corresponding start tag and vice versa 
        //5.  check for empty tags
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = null;
        Hashtable<String, Integer> startTags = new Hashtable<String, Integer>();
        Hashtable<String, Integer> endTags = new Hashtable<String, Integer>();
        boolean allOK = true;
        String endA = new String();
        String startA = new String();
        int lineCounter = 1;
        while ((line = reader.readLine()) != null) {
            if (line.contains(endAnnotation)) {
                //get the end id
                endA = line.trim().substring(endAnnotation.length()).trim();
                //check if the tag is not empty 
                if (endA.length() == 0) {
                    //TODO replace with log4j
                    System.out.println("[" + lineCounter + "] WARNING: Empty tag found at " + "[" +
                        lineCounter + "]. File [" + file + "] will not be parsed and some code parts may" +
                        " not appear in the final document");
                    allOK = false;

                }
                //check if the tags are unique
                if (endTags.containsKey(endA)) {
                    //TODO replace with log4j
                    System.out.println("[" + lineCounter + "] WARNING: Duplicate stop tags [" + endA +
                        "] at " + "[" + lineCounter + "] and [" + endTags.get(endA) + "] " + ". File [" +
                        file + "] will not be parsed and some code parts may" +
                        " not appear in the final document");
                    allOK = false;
                }
                endTags.put(endA, lineCounter);
            }
            if (line.contains(startAnnotation)) {
                //get the start id 
                startA = line.trim().substring(startAnnotation.length()).trim();
                if (startA.length() == 0) {
                    //TODO replace with log4j
                    System.out.println("[" + lineCounter + "] WARNING: Empty tag found at " + "[" +
                        lineCounter + "]. File [" + file + "] will not be parsed and some code parts may" +
                        " not appear in the final document");
                    allOK = false;
                }
                if (startTags.containsKey(startA)) {
                    //TODO replace with log4j
                    System.out.println("[" + lineCounter + "] WARNING: Duplicate start tags [" + startA +
                        "] at " + "[" + lineCounter + "] and [" + startTags.get(startA) + "] " + ". File [" +
                        file + "] will not be parsed and some code parts may" +
                        " not appear in the final document");
                    allOK = false;
                }
                startTags.put(startA, lineCounter);
            }
            lineCounter++;
        }
        //TODO log the tags found with log4j
        //		if (startTags.size() > 0 ) System.out.println(startTags.keySet().toString());
        //		if (endTags.size() > 0 ) System.out.println(endTags.keySet().toString());

        //check if there are only pairs of tags (no extra single ones)
        //and start tags are before end tags
        //remove all the correct tags  and orphaned tags from the hashtable 
        //and report on what's left because there can 
        //be end tags without start tags
        for (String tag : startTags.keySet()) {
            //check for existence
            if (endTags.get(tag) == null) {
                //TODO replace with log4j
                System.out.println("[" + startTags.get(tag) + "] WARNING: Orphaned start tag [" + tag +
                    "] found at line:" + "[" + startTags.get(tag) + "]. File [" + file +
                    "] will not be parsed and some code parts may" + "not appear in the final document.");
                allOK = false;
            } else {
                //check for order
                if (endTags.get(tag) <= startTags.get(tag)) {
                    //TODO replace with log4j
                    System.out.println("[" + endTags.get(tag) + "," + startTags.get(tag) +
                        "] WARNING: End tag [" + tag + "] found before start tag. End tag is at line:" + "[" +
                        endTags.get(tag) + "] and start tag is at line [" + startTags.get(tag) + "] " +
                        ". File [" + file + "] will not be parsed and some code parts may" +
                        " not appear in the final document");
                    allOK = false;
                }
                if (endTags.get(tag) != null)
                    endTags.remove(tag);
            }
        }

        //report the error lines for the orphaned end tags
        if (endTags.size() != 0)
            for (String tag : endTags.keySet()) {
                //TODO replace with log4j
                System.out.println("[" + endTags.get(tag) + "] WARNING: Orphaned end tag [" + tag +
                    "] found at line:" + "[" + endTags.get(tag) + "]. File [" + file +
                    "] will not be parsed and some code parts may" + "not appear in the final document.");
                allOK = false;
            }
        if (!allOK)
            return false;

        return true;

    }

    //considers the file valid as it has been checked 
    //by fileIsValid before parsing
    public void extractSnippets(File file) throws Exception {
        //System.out.println("Try to extract from:" + file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = null;
        BufferedWriter writer = null;
        Hashtable<String, BufferedWriter> writers = new Hashtable<String, BufferedWriter>();

        while ((line = reader.readLine()) != null) {

            //if writers still exist, eg the last end annotation hasn't been reached add to the snippet files
            if (writers.size() > 0)
                if (line.contains(endAnnotation)) {
                    //close the writer corresponding to the end annotation
                    String endA = line.trim().substring(endAnnotation.length()).trim();
                    writer = writers.get(endA);
                    writer.flush();
                    writer.close();
                    writers.remove(endA);
                    continue;
                } else {
                    //iterate through all the writers and write in the files
                    //skip the lines that contain annotations (we might have imbricated or included annotations)
                    for (BufferedWriter buffer : writers.values()) {
                        if (!line.contains(startAnnotation) && !line.contains(startAnnotation))
                            buffer.append(line);
                        buffer.newLine();
                    }
                }
            //if new start annotation encountered add a new file and writer
            if (line.contains(startAnnotation)) {
                //get only the id 
                String startA = line.trim().substring(startAnnotation.length()).trim();
                //TODO check if startA can be a valid file name
                writers.put(startA, createFile(startA));
            }
        }
        reader.close();
    }

    public BufferedWriter createFile(String file) throws Exception {

        File targetFile = new File(targetDir, file);
        if (targetFile.exists()) {
            //TODO replace by log4j
            System.out
                    .println("WARNING: File " +
                        targetFile +
                        " already exists and it will be overwritten. " +
                        " Either the directory has not been emptied or there are global duplicate tags. The file(tag) name is" +
                        ":" + file);
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile));
        return writer;
    }

    //TODO input checking
    public static void main(String[] args) throws Exception {

        File sourceDir = new File(args[0]);
        File targetDir = new File(args[1]);

        //System.out.println("Processing starting from "+ sourceDir +
        //		" and outputting to " + targetDir);
        Snippetizer parser = new Snippetizer(new File(sourceDir.getAbsolutePath()), targetDir);
        parser.startExtraction(sourceDir);

    }

}
