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

import java.io.File;
import java.util.ResourceBundle;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * Entry point class for extracting snippets from code files.
 * Snippets will be identified by the id next to the tag check
 * SnippetExtractorFactory.java for tags) and placed in the 
 * ouput directory. To use in the docbook XML just use the
 * <programlisting> tag and include the needed snippet file. 
 * 
 * @author vjuresch
 *
 */
public class Snippetizer {
    private static Logger logger = Logger.getLogger(Snippetizer.class.getName());

    protected File targetDir;
    protected File rootDir;
    private String[] fileTypes = { ".java", ".xml", ".fractal" };

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
        // List the source directory. If the file is a dir recurse,
        // if the file is a java file check for annotations
        // otherwise ignore
        File[] elements = dir.listFiles();
        for (File file : elements) {
            if (file.isDirectory()) {
                startExtraction(file);
            } else
                for (String extension : fileTypes)
                    if ((file.toString().lastIndexOf(extension) + extension.length() == file.toString()
                            .length()) &&
                        !file.getName().equals("SnippetExtractorFactory.java")) {
                        //get the correct extractor and start it 
                        SnippetExtractorFactory.getExtractor(file, targetDir).run();
                    } // fi
        } // rof
    }

    public static void main(String[] args) {
        //TODO configure externally
        logger.setLevel(Level.ALL);
        if (args.length >= 2) {
            File sourceDir = new File(args[0]);
            File targetDir = new File(args[1]);
            if (sourceDir.isDirectory() && targetDir.isDirectory()) {
                logger.info("Processing starting from: " + sourceDir + ", outputting to: " + targetDir);
                Snippetizer parser = new Snippetizer(new File(sourceDir.getAbsolutePath()), targetDir);
                parser.startExtraction(sourceDir);
                logger.info("Snippet parsing completed.");
                return;
            }
        }
        System.err.println("The snippet parser takes "
            + "two parameters. The first one is the source directory and the second "
            + "is the target directory. The source directory will be traversed recursively "
            + "and every file with the specified extensions (check doc.snippets.SnippetExtractorFactory.java"
            + " will be parsed for snippet parts");
        logger.error("Not enough arguments or arguments are not directories.");
    }

}
