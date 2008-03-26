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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * This class is responsible with creating the 
 * appropriate snippet parser depending on the file. 
 * 
 * How the tags look like is also defined here. 
 * @author The ProActive Team
 *
 */
public class SnippetExtractorFactory {
    private static Logger logger = Logger.getLogger(SnippetExtractorFactory.class.getName());

    public static String startAnnotationJava = "@snippet-start";
    public static String endAnnotationJava = "@snippet-end";

    public static String startAnnotationXML = "<!--@snippet-start";
    public static String endAnnotationXML = "<!--@snippet-end";

    public static String startAnnotationFractal = "<!--@snippet-start";
    public static String endAnnotationFractal = "<!--@snippet-end";

    /**
     * This class will not be instantiated 
     */
    private SnippetExtractorFactory() {
    };

    /**
     * Returns an appropriate parser chosen according to the file type.
     * 
     * @param file file to be parsed
     * @param targetDir where the snippets will be placed
     * @return a parser for the file
     */
    public static SnippetExtractor getExtractor(File file, File targetDir) {
        //TODO configure externally
        logger.setLevel(Level.INFO);
        if (file.toString().endsWith(".java")) {
            logger.debug("Java snippet parser started for file : " + file + " and target directory " +
                targetDir + ". The annotations used are: " + startAnnotationJava + " and " +
                endAnnotationJava);
            return new JavaSnippetExtractor(file, targetDir, startAnnotationJava, endAnnotationJava, null,
                null);
        }
        if (file.toString().endsWith(".xml")) {
            logger.debug("XML snippet parser started for file : " + file + " and target directory " +
                targetDir + ". The annotations used are: " + startAnnotationJava + " and " +
                endAnnotationJava);
            return new XMLSnippetExtractor(file, targetDir, startAnnotationJava, endAnnotationJava, null,
                null);
        }
        if (file.toString().endsWith(".fractal")) {
            logger.debug("Fractal snippet parser started for file : " + file + " and target directory " +
                targetDir + ". The annotations used are: " + startAnnotationJava + " and " +
                endAnnotationJava);
            return new XMLSnippetExtractor(file, targetDir, startAnnotationJava, endAnnotationJava, null,
                null);
        }
        return null;
    }

    /**
     * @return the startAnnotationJava
     */
    public static String getStartAnnotationJava() {
        return startAnnotationJava;
    }

    /**
     * @param startAnnotationJava the startAnnotationJava to set
     */
    public static void setStartAnnotationJava(String startAnnotationJava) {
        SnippetExtractorFactory.startAnnotationJava = startAnnotationJava;
    }

    /**
     * @return the endAnnotationJava
     */
    public static String getEndAnnotationJava() {
        return endAnnotationJava;
    }

    /**
     * @param endAnnotationJava the endAnnotationJava to set
     */
    public static void setEndAnnotationJava(String endAnnotationJava) {
        SnippetExtractorFactory.endAnnotationJava = endAnnotationJava;
    }

    /**
     * @return the startAnnotationXML
     */
    public String getStartAnnotationXML() {
        return startAnnotationXML;
    }

    /**
     * @param startAnnotationXML the startAnnotationXML to set
     */
    public void setStartAnnotationXML(String startAnnotationXML) {
        this.startAnnotationXML = startAnnotationXML;
    }

    /**
     * @return the endAnnotationXML
     */
    public String getEndAnnotationXML() {
        return endAnnotationXML;
    }

    /**
     * @param endAnnotationXML the endAnnotationXML to set
     */
    public void setEndAnnotationXML(String endAnnotationXML) {
        this.endAnnotationXML = endAnnotationXML;
    }

    public static String getStartAnnotationFractal() {
        return startAnnotationFractal;
    }

    public static void setStartAnnotationFractal(String startAnnotationFractal) {
        SnippetExtractorFactory.startAnnotationFractal = startAnnotationFractal;
    }

}
