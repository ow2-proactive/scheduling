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
import java.io.IOException;

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
public final class SnippetExtractorFactory {
    private static Logger logger = Logger.getLogger(SnippetExtractorFactory.class.getName());

    static String startAnnotationFractal = "<!--@snippet-start";
    static String endAnnotationFractal = "<!--@snippet-end";

    static String startAnnotationJava = "@snippet-start";
    static String endAnnotationJava = "@snippet-end";

    static String startAnnotationXML = "<!--@snippet-start";
    static String endAnnotationXML = "<!--@snippet-end";

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
    public static SnippetExtractor getExtractor(final File file, final File targetDir) throws IOException {
        //TODO configure externally
        SnippetExtractorFactory.logger.setLevel(Level.INFO);
        //return a JavaSnippetExtractor for parsing java files
        if (file.toString().endsWith(".java")) {
            SnippetExtractorFactory.logger.debug("Java snippet parser started for file : " + file +
                " and target directory " + targetDir + ". The annotations used are: " +
                SnippetExtractorFactory.startAnnotationJava + " and " +
                SnippetExtractorFactory.endAnnotationJava);
            return new JavaSnippetExtractor(file, targetDir, SnippetExtractorFactory.startAnnotationJava,
                SnippetExtractorFactory.endAnnotationJava);
        }
        //return a XMLSnippetExtractor for parsing XML files
        if (file.toString().endsWith(".xml")) {

            SnippetExtractorFactory.logger.debug("XML snippet parser started for file : " + file +
                " and target directory " + targetDir + ". The annotations used are: " +
                SnippetExtractorFactory.startAnnotationJava + " and " +
                SnippetExtractorFactory.endAnnotationJava);
            return new XMLSnippetExtractor(file, targetDir, SnippetExtractorFactory.startAnnotationXML,
                SnippetExtractorFactory.endAnnotationXML);
        }
        //return a XMLSnippetExtractor for parsing fractal files (XML format)
        if (file.toString().endsWith(".fractal")) {
            SnippetExtractorFactory.logger.debug("Fractal snippet parser started for file : " + file +
                " and target directory " + targetDir + ". The annotations used are: " +
                SnippetExtractorFactory.startAnnotationXML + " and " +
                SnippetExtractorFactory.endAnnotationXML);
            return new XMLSnippetExtractor(file, targetDir, SnippetExtractorFactory.startAnnotationJava,
                SnippetExtractorFactory.endAnnotationJava);
        }
        throw new IOException();
    }

    /**
     * @return the startAnnotationJava
     */
    public static String getStartAnnotationJava() {
        return SnippetExtractorFactory.startAnnotationJava;
    }

    /**
     * @param startAnnotationJava the startAnnotationJava to set
     */
    public static void setStartAnnotationJava(final String startAnnotationJava) {
        SnippetExtractorFactory.startAnnotationJava = startAnnotationJava;
    }

    /**
     * @return the endAnnotationJava
     */
    public static String getEndAnnotationJava() {
        return SnippetExtractorFactory.endAnnotationJava;
    }

    /**
     * @param endAnnotationJava the endAnnotationJava to set
     */
    public static void setEndAnnotationJava(final String endAnnotationJava) {
        SnippetExtractorFactory.endAnnotationJava = endAnnotationJava;
    }

    /**
     * @return the startAnnotationXML
     */
    public String getStartAnnotationXML() {
        return SnippetExtractorFactory.startAnnotationXML;
    }

    /**
     * @param startAnnotationXML the startAnnotationXML to set
     */
    public void setStartAnnotationXML(final String startAnnotationXML) {
        SnippetExtractorFactory.startAnnotationXML = startAnnotationXML;
    }

    /**
     * @return the endAnnotationXML
     */
    public String getEndAnnotationXML() {
        return SnippetExtractorFactory.endAnnotationXML;
    }

    /**
     * @param endAnnotationXML the endAnnotationXML to set
     */
    public void setEndAnnotationXML(final String endAnnotationXML) {
        SnippetExtractorFactory.endAnnotationXML = endAnnotationXML;
    }

    public static String getStartAnnotationFractal() {
        return SnippetExtractorFactory.startAnnotationFractal;
    }

    public static void setStartAnnotationFractal(final String startAnnotationFractal) {
        SnippetExtractorFactory.startAnnotationFractal = startAnnotationFractal;
    }

}
