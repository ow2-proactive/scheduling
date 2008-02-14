/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): Vasile Jureschi
 *
 * ################################################################
 */
package doc.snippets;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * This class is responsible with the actual parsing of the files. 
 * @author vjuresch
 *
 */
public abstract class SnippetExtractor implements Runnable {
    protected static Logger logger = Logger.getLogger(SnippetExtractor.class.getName());

    private String startAnnotation = new String();
    private String endAnnotation = new String();;

    //TODO implement areas that are excluded from snippets
    private String startExclusion = new String();
    private String endExclusion = new String();

    protected File target;
    protected File targetDirectory;

    /**
     * @param f file to be parsed
     * @param startA snippet start tag
     * @param endA snippet end tag
     * @param startE area of exclusion form snippet start tag
     * @param endE are of exclusion from snippet end tag
     */
    public SnippetExtractor(File f, File targetDir, String startA, String endA, String startE, String endE) {
        //TODO configure externally
        logger.setLevel(Level.INFO);
        target = f;
        startAnnotation = startA;
        endAnnotation = endA;
        startExclusion = startE;
        endExclusion = endE;
        targetDirectory = targetDir;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            //check if the file is valid and then parse
            if (fileIsValid())
                extractSnippets();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Extraction error for file: " + target);
        }
    }

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
    public boolean fileIsValid() throws Exception {
        //conditions for validity
        //1.  same number of start and end tags
        //2.  start tags always have a higher index then corresponding end tags (are before)
        //3.  no duplicate start or end tags (should be checked globally somehow)
        //4.  an end tag has a corresponding start tag and vice versa 
        //5.  check for empty tags
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(target)));
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
                endA = extractAnnotation(line, endAnnotation);
                //check if the tag is not empty 
                if (endA.length() == 0) {
                    logger.error("[" + lineCounter + "]  Empty tag found at " + "[" + lineCounter +
                        "]. File [" + target + "] will not be parsed and some code parts may" +
                        " not appear in the final document");
                    allOK = false;

                }
                //check if the tags are unique
                if (endTags.containsKey(endA)) {
                    logger.error("[" + lineCounter + "]  Duplicate stop tags [" + endA + "] at " + "[" +
                        lineCounter + "] and [" + endTags.get(endA) + "] " + ". File [" + target +
                        "] will not be parsed and some code parts may" + " not appear in the final document");
                    allOK = false;
                }
                endTags.put(endA, lineCounter);
            }
            if (line.contains(startAnnotation)) {
                //get the start id 
                startA = extractAnnotation(line, startAnnotation);
                if (startA.length() == 0) {
                    logger.error("[" + lineCounter + "]  Empty tag found at " + "[" + lineCounter +
                        "]. File [" + target + "] will not be parsed and some code parts may" +
                        " not appear in the final document");
                    allOK = false;
                }
                if (startTags.containsKey(startA)) {
                    logger.error("[" + lineCounter + "]  Duplicate start tags [" + startA + "] at " + "[" +
                        lineCounter + "] and [" + startTags.get(startA) + "] " + ". File [" + target +
                        "] will not be parsed and some code parts may" + " not appear in the final document");
                    allOK = false;
                }
                startTags.put(startA, lineCounter);
            }
            lineCounter++;
        }
        if (startTags.size() > 0)
            logger.debug("Start tags extracted :" + startTags.keySet().toString());
        if (endTags.size() > 0)
            logger.debug("End tags extracted :" + endTags.keySet().toString());

        //check if there are only pairs of tags (no extra single ones)
        //and start tags are before end tags
        //remove all the correct tags  and orphaned tags from the hashtable 
        //and report on what's left because there can 
        //be end tags without start tags
        for (String tag : startTags.keySet()) {
            //check for existence
            if (endTags.get(tag) == null) {
                logger.error("[" + startTags.get(tag) + "]  Orphaned start tag [" + tag + "] found at line:" +
                    "[" + startTags.get(tag) + "]. File [" + target +
                    "] will not be parsed and some code parts may" + "not appear in the final document.");
                allOK = false;
            } else {
                //check for order
                if (endTags.get(tag) <= startTags.get(tag)) {
                    logger.error("[" + endTags.get(tag) + "," + startTags.get(tag) + "]  End tag [" + tag +
                        "] found before start tag. End tag is at line:" + "[" + endTags.get(tag) +
                        "] and start tag is at line [" + startTags.get(tag) + "] " + ". File [" + target +
                        "] will not be parsed and some code parts may" + " not appear in the final document");
                    allOK = false;
                }
                if (endTags.get(tag) != null)
                    endTags.remove(tag);
            }
        }

        //report the error lines for the orphaned end tags
        if (endTags.size() != 0)
            for (String tag : endTags.keySet()) {
                logger.error("[" + endTags.get(tag) + "]  Orphaned end tag [" + tag + "] found at line:" +
                    "[" + endTags.get(tag) + "]. File [" + target +
                    "] will not be parsed and some code parts may" + "not appear in the final document.");
                allOK = false;
            }
        if (!allOK)
            return false;
        return true;

    }

    //considers the file valid as it has been checked 
    //by fileIsValid before parsing
    private void extractSnippets() throws Exception {
        //System.out.println("Try to extract from:" + file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(target)));
        String line = null;
        BufferedWriter writer = null;
        Hashtable<String, BufferedWriter> writers = new Hashtable<String, BufferedWriter>();
        String endA = null;
        String startA = null;
        while ((line = reader.readLine()) != null) {

            //if writers still exist, eg the last end annotation hasn't been reached add to the snippet files
            if (writers.size() > 0)
                if (line.contains(endAnnotation)) {
                    //close the writer corresponding to the end annotation
                    endA = extractAnnotation(line, endAnnotation);
                    writer = writers.get(endA);
                    writer.flush();
                    writer.close();
                    writers.remove(endA);
                    continue;
                } else {
                    //iterate through all the writers and write in the files
                    //skip the lines that contain annotations (we might have imbricated or included annotations)
                    for (BufferedWriter buffer : writers.values()) {
                        if (!line.contains(startAnnotation) && !line.contains(endAnnotation))
                            buffer.append(line);
                        buffer.newLine();
                    }
                }
            //if new start annotation encountered add a new file and writer
            if (line.contains(startAnnotation)) {
                //get only the id 
                startA = extractAnnotation(line, startAnnotation);
                //TODO check if startA can be a valid file name
                writers.put(startA, createFile(startA));
            }
        }
        reader.close();
    }

    private BufferedWriter createFile(String file) {

        File targetFile = new File(targetDirectory, file);
        logger.debug("Creating file:" + targetFile.toString());
        if (targetFile.exists()) {
            logger
                    .warn(" File " +
                        targetFile +
                        " already exists and it will be overwritten. " +
                        " Either the directory has not been emptied or there are global duplicate tags. The file(tag) name is" +
                        ":" + file);
        }
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(targetFile));
            return writer;
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("File " + targetFile + " could not be created.");
        }
        return null;
    }

    /**
     * This method is to be implemented by the subclasses responsible
     * for parsing different types of file. The way the snippet name is
     * extracted is left at the discretion of those classes.  
     * @param line The line from which the snippet name will be extracted
     * @param annotation the annotation tag
     * @return
     */
    public abstract String extractAnnotation(String line, String annotation);

}
