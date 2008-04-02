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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * This class is responsible with the actual parsing of the files.
 * 
 * @author vjuresch
 * 
 */
public abstract class SnippetExtractor implements Runnable {
    private static final String FILE_EXTENSION = ".snip";

    protected static Logger logger = Logger.getLogger(SnippetExtractor.class.getName());

    private final String startAnnotation;
    private final String endAnnotation;
    // TODO implement areas that are excluded from snippets
    private final String startExclusion;
    private final String endExclusion;

    protected final File target;
    protected final File targetDirectory;

    private BufferedReader reader;

    /**
     * Creates a snippet extractor with the corresponding tags
     * 
     * @param file
     *            file to be parsed
     * @param targetDir
     *            directory to save to
     * @param startA
     *            snippet start tag
     * @param endA
     *            snippet end tag
     * @param startE
     *            start area of exclusion form snippet start tag (not
     *            implemented)
     * @param endE
     *            end are of exclusion from snippet end tag (not implemented)
     */
    public SnippetExtractor(final File file, final File targetDir, final String startA, final String endA,
            final String startE, final String endE) {
        // TODO configure externally
        SnippetExtractor.logger.setLevel(Level.INFO);
        this.target = file;
        this.startAnnotation = startA;
        this.endAnnotation = endA;
        this.startExclusion = startE;
        this.endExclusion = endE;
        this.targetDirectory = targetDir;
    }

    /**
     * Creates a snippet extractor with the corresponding tags
     * 
     * @param file
     *            file to be parsed
     * @param targetDir
     *            directory to save to
     * @param startA
     *            snippet start tag
     * @param endA
     *            snippet end tag
     * 
     */
    public SnippetExtractor(final File file, final File targetDir, final String startA, final String endA) {
        this(file, targetDir, startA, endA, null, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    /**
     * Checks if the file supplied is valid and contains tags
     * and if so tries to extract the snippets.
     */
    public void run() {
        try {
            this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.target)));
            // check if the file is valid and then parse
            if (this.fileIsValid()) {
                SnippetExtractor.logger.debug("File is valid, trying to extract: " + this.target);
                this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.target)));
                this.extractSnippets();
            }
        } catch (final IOException ioExcep) {
            ioExcep.printStackTrace();
            SnippetExtractor.logger.error("Extraction error for file: " + this.target + " " +
                ioExcep.getMessage());
        }
    }

    /**
     * Checks that for each start tag there is a corresponding end tag and there
     * are no stop tags before end tags. Tags can be imbricated.
     * 
     * Validity conditions 1. same number of start and end tags 2. start tags
     * always have a higher index then corresponding end tags (are before) 3. no
     * duplicate start or end tags (should be checked globally somehow) 4. an
     * end tag has a corresponding start tag and vice versa 5. check for empty
     * tags
     * 
     * @return a boolean value saying if the file is valid or not
     * @throws IOException file error
     */
    private boolean fileIsValid() throws IOException {
        // conditions for validity
        // 1. same number of start and end tags
        // 2. start tags always have a higher index then corresponding end tags
        // (are before)
        // 3. no duplicate start or end tags (should be checked globally
        // somehow)
        // 4. an end tag has a corresponding start tag and vice versa
        // 5. check for empty tags
        String line;
        final HashMap<String, Integer> startTags = new HashMap<String, Integer>();
        final HashMap<String, Integer> endTags = new HashMap<String, Integer>();
        boolean fileValid = true;
        String endA;
        String startA;
        // counts the line number of the tag in the file
        int lineCounter = 1;
        // counts the tags in the file 0 means the file has no tags and will not
        // be parsed
        int tagCounter = 0;
        line = this.reader.readLine();
        while ((line != null) && fileValid) {
            if (line.contains(this.endAnnotation)) {
                // get the end id
                endA = this.extractAnnotation(line, this.endAnnotation);
                SnippetExtractor.logger.debug("Found end tag [" + endA + "]" + "at line " + lineCounter);
                // check if the tag is not empty
                if (endA.length() == 0) {
                    SnippetExtractor.logger.error("[" + lineCounter + "]  Empty tag found at " + "[" +
                        lineCounter + "]. File [" + this.target +
                        "] will not be parsed and some code parts may" + " not appear in the final document");
                    fileValid = false;
                }
                // check if the tags are unique
                if (endTags.containsKey(endA)) {
                    SnippetExtractor.logger.error("[" + lineCounter + "]  Duplicate stop tags [" + endA +
                        "] at " + "[" + lineCounter + "] and [" + endTags.get(endA) + "] " + ". File [" +
                        this.target + "] will not be parsed and some code parts may" +
                        " not appear in the final document");
                    fileValid = false;
                }
                endTags.put(endA, lineCounter);
            }
            if (line.contains(this.startAnnotation)) {
                // we count for tags only here since end annotation
                // are invalid without start annotations
                tagCounter++;
                // get the start id
                startA = this.extractAnnotation(line, this.startAnnotation);
                SnippetExtractor.logger.debug("Found start tag [" + startA + "]" + "at line " + lineCounter);
                if (startA.length() == 0) {
                    SnippetExtractor.logger.error("[" + lineCounter + "]  Empty tag found at " + "[" +
                        lineCounter + "]. File [" + this.target +
                        "] will not be parsed and some code parts may" + " not appear in the final document");
                    fileValid = false;
                }
                if (startTags.containsKey(startA)) {
                    SnippetExtractor.logger.error("[" + lineCounter + "]  Duplicate start tags [" + startA +
                        "] at " + "[" + lineCounter + "] and [" + startTags.get(startA) + "] " + ". File [" +
                        this.target + "] will not be parsed and some code parts may" +
                        " not appear in the final document");
                    fileValid = false;
                }
                startTags.put(startA, lineCounter);
            }
            lineCounter++;
            line = this.reader.readLine();
        }
        if (startTags.size() > 0)
            SnippetExtractor.logger.debug("Start tags extracted :" + startTags.keySet().toString());
        if (endTags.size() > 0)
            SnippetExtractor.logger.debug("End tags extracted :" + endTags.keySet().toString());

        // check if there are only pairs of tags (no extra single ones)
        // and start tags are before end tags
        // remove all the correct tags and orphaned tags from the HashMap
        // and report on what's left because there can
        // be end tags without start tags
        String startKey;
        Integer startValue;
        String endKey;
        Integer endValue;
        for (final Map.Entry<String, Integer> tag : startTags.entrySet()) {
            startKey = tag.getKey();
            startValue = tag.getValue();
            endKey = startKey;
            endValue = endTags.get(endKey);
            // check for existence
            if (endValue == null) {
                SnippetExtractor.logger.error("[" + startValue + "]  Orphaned start tag [" + startKey +
                    "] found at line:" + "[" + startValue + "]. File [" + this.target +
                    "] will not be parsed and some code parts may" + "not appear in the final document.");
                fileValid = false;
            } else {
                // check for order
                if (endValue <= startValue) {
                    SnippetExtractor.logger.error("[" + endValue + "," + startValue + "]  End tag [" +
                        endKey + "] found before start tag. End tag is at line:" + "[" + endValue +
                        "] and start tag is at line [" + startValue + "] " + ". File [" + this.target +
                        "] will not be parsed and some code parts may" + " not appear in the final document");
                    fileValid = false;
                }
                if (endValue != null)
                    endTags.remove(endKey);
            }
            line = this.reader.readLine();
        }
        // report the error lines for the orphaned end tags
        if (endTags.size() != 0)
            for (final Map.Entry<String, Integer> tag : endTags.entrySet()) {
                endKey = tag.getKey();
                endValue = tag.getValue();
                SnippetExtractor.logger.error("[" + endValue + "]  Orphaned end tag [" + endKey +
                    "] found at line:" + "[" + endValue + "]. File [" + this.target +
                    "] will not be parsed and some code parts may" + "not appear in the final document.");
                fileValid = false;
            }
        if ((tagCounter == 0) || !fileValid)
            return false;
        return true;

        // the method does not return immediately on finding an error
        // in order to report as many errors as possible in on try (makes fixing
        // the errors faster)

    }

    // considers the file valid as it has been checked
    // by fileIsValid before parsing
    /**
     * @throws IOException
     */
    private void extractSnippets() throws IOException {
        String line;
        BufferedWriter writer;
        final HashMap<String, BufferedWriter> writers = new HashMap<String, BufferedWriter>();
        //holds the number of whitespace to be removed from a file
        final HashMap<String, Integer> whiteSpaceToRemove = new HashMap<String, Integer>();
        String endA;
        String startA;
        line = this.reader.readLine();
        while (line != null) {
            //if we found an end annotation close the coresponding writer
            if ((writers.size() > 0) && (line.contains(this.endAnnotation))) {
                // close the writer corresponding to the end annotation
                endA = this.extractAnnotation(line, this.endAnnotation);
                writer = writers.get(endA);
                SnippetExtractor.logger.debug("Removing --- " + endA + "  line --- " + line);
                assert endA != null;
                writer.flush();
                writer.close();
                writers.remove(endA);
                SnippetExtractor.logger.debug("---- Writers left after removal: " + writers);
                // format the file (remove whitespaces)
                this.formatFile(endA, whiteSpaceToRemove.get(endA));
                // remove the whitespace count form the whitespace count vector
                whiteSpaceToRemove.remove(endA);
            } //end end annotation if
            // if writers still exist, eg the last end annotation
            // hasn't been reached add to the snippet files
            if ((writers.size() > 0) && (!line.contains(this.endAnnotation)) &&
                (!line.contains(this.startAnnotation))) {
                // iterate through all the writers and write in the files
                // skip if the line contains an annotation (we might
                // have imbricated or included annotations)
                for (final BufferedWriter buffer : writers.values()) {
                    if (!line.contains(this.startAnnotation) && !line.contains(this.endAnnotation)) {
                        buffer.append(line);
                    }
                    buffer.newLine();
                    // choose the smallest value (closest to the left) between
                    // the currently recorded white space and the one in the current line
                    // the minimum from all the lines in one file is the amount we have to
                    // move the text to the left
                    Integer value;
                    for (final Map.Entry<String, Integer> currentWhiteSpace : whiteSpaceToRemove.entrySet()) {
                        value = Math.min(line.length() - line.trim().length(), currentWhiteSpace.getValue());
                        whiteSpaceToRemove.put(currentWhiteSpace.getKey(), value);
                    }
                }
            } //end no annotation if
            // if new start annotation encountered add a new file and writer
            if (line.contains(this.startAnnotation)) {
                // get only the id
                startA = this.extractAnnotation(line, this.startAnnotation);
                // TODO check if startA can be a valid file name
                final File targetFile = new File(this.targetDirectory, startA);
                //if file exist log a warning, otherwise proceed as normal
                if (targetFile.exists()) {
                    SnippetExtractor.logger.warn(" File " + targetFile +
                        " already exists and it will NOT be overwritten. " +
                        " Either the directory has not been emptied" +
                        " or there are global duplicate tags. The file(tag) name is" + ":" + startA +
                        ". The tag has be read from file " + this.target);
                } else {
                    SnippetExtractor.logger.debug("Adding ----" + startA + " line --- " + line);
                    assert startA != null;
                    writers.put(startA, this.createFile(startA));
                    SnippetExtractor.logger.info("File [" + startA + "] created.");
                    SnippetExtractor.logger.debug("++++ Writers after adding:" + writers);
                }
                // create a new whitespace entry with a maximum
                // value (we are looking for the minimum value)
                whiteSpaceToRemove.put(startA, Integer.MAX_VALUE);
            } //end start annotation if
            line = this.reader.readLine();
        } //end while
        this.reader.close();
    }

    /**
     * Creates a new file with the specified name 
     * 
     * @param file the file name to be created
     * @return a BufferedWriter representing the created file
     */
    private BufferedWriter createFile(final String file) {

        final File targetFile = new File(this.targetDirectory, file);
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(targetFile));
            SnippetExtractor.logger.debug("Creating: " + targetFile);
            return writer;
        } catch (final IOException e) {
            e.printStackTrace();
            SnippetExtractor.logger.error("File " + targetFile + " could not be created.");
        }
        return null;
    }

    /**
     * Formats the file by removing an equal amount of whitespaces from the
     * beginning of all the lines. The number of whitespaces removed is equal to
     * the smallest number of whitespace that can be found on a beginning of a
     * line (e.g. on the line closest to the left edge of the screen).
     * 
     * @param file  the file to be formated
     * @param blanksToRemove the number of whitespace to remove from each line
     */
    private void formatFile(final String file, final int blanksToRemove) {
        final File parsedFile = new File(this.targetDirectory, file);
        try {
            final BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(
                parsedFile)));
            String line = null;
            final File outFile = new File(this.targetDirectory, file + SnippetExtractor.FILE_EXTENSION);

            final BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
            String whiteSpaceToAdd;
            SnippetExtractor.logger.debug("Input file :" + parsedFile + " output file " + outFile +
                " to remove " + blanksToRemove);
            int whiteSpacelength;
            while ((line = fileReader.readLine()) != null) {
                // calculate the white space on this line
                whiteSpacelength = line.length() - line.trim().length();
                whiteSpaceToAdd = "";
                for (int i = 1; i < whiteSpacelength - blanksToRemove; i++)
                    // create a string of whitespace length - the amount to be
                    // removed
                    whiteSpaceToAdd = whiteSpaceToAdd.concat(" ");
                // add the trimmed line to the whitespace and write to the file
                writer.write(whiteSpaceToAdd.concat(line.trim()));
                writer.newLine();
            }
            writer.close();
            fileReader.close();
            // remove temporary file
            if (!parsedFile.delete())
                throw new IOException("Temporary files could not be deleted");
        } catch (final IOException ioExcep) {
            SnippetExtractor.logger.error("File I/O exception");
            SnippetExtractor.logger.error(ioExcep.getMessage());
        }
    }

    /**
     * This method is to be implemented by the subclasses responsible for
     * parsing different types of file. The way the snippet name is extracted is
     * left at the discretion of those classes.
     * 
     * @param line
     *            The line from which the snippet name will be extracted
     * @param annotation
     *            the annotation tag
     * @return the snippet name
     */
    public abstract String extractAnnotation(String line, String annotation);

}
