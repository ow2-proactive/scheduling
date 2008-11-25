/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package doc.snippets;

import java.io.File;


/**
 * Extracts snippets from a java file.
 * @author vjuresch
 *
 */
public class JavaSnippetExtractor extends SnippetExtractor {

    /**
     * Constructs a new JavaSnippetExtractor that
     * will parse the file and output in the 
     * target directory the parts of the code between 
     * the annotations.
     * 
     * @param file  	file to extract from 
     * @param targetDir	directory to save the file to 
     * @param startA	start annotation
     * @param endA		end annotation
     */
    public JavaSnippetExtractor(final File file, final File targetDir, final String startA, final String endA) {
        super(file, targetDir, startA, endA);
    }

    /**
     * Constructs a new JavaSnippetExtractor that
     * will parse the file and output in the 
     * target directory the parts of the code between 
     * the annotations. It will not include the
     * code between the exclude tags.
     * 
     * @param file  	file to extract from 
     * @param targetDir	directory to save the file to 
     * @param startA	start annotation
     * @param endA		end annotation		
     * @param startE	start exclusion annotation (not implemented) - beginning of exclusion from
     * 						included part  
     * @param endE		end exclusion annotation - ending of exclusion from included part
     */
    public JavaSnippetExtractor(final File file, final File targetDir, final String startA,
            final String endA, final String startE, final String endE) {
        super(file, targetDir, startA, endA, startE, endE);
    }

    /* (non-Javadoc)
     * @see doc.snippets.SnippetExtractor#extractAnnotation(java.lang.String, java.lang.String)
     *
     */
    /**
     * @param line the line from which to extract the snippet name
     * @param annotation the annotation on the line
     * 
     * @return returns only the name of the snippet 
     */
    @Override
    public String extractAnnotation(final String line, final String annotation) {
        //snippets are in the form
        String name = line;
        name = name.replace("/", " ");
        name = name.replace(annotation, " ");
        name = name.trim();
        SnippetExtractor.logger.debug("Extracted the " + name + " snippet from " + target);
        return name;
    }
}
