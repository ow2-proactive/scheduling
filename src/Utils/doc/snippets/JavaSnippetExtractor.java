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


/**
 * Extracts snippets from a java file.
 * @author The ProActive Team
 *
 */
public class JavaSnippetExtractor extends SnippetExtractor {

    /**
     * @param f
     * @param targetDir
     * @param startA
     * @param endA
     * @param startE
     * @param endE
     */
    public JavaSnippetExtractor(File f, File targetDir, String startA, String endA, String startE, String endE) {
        super(f, targetDir, startA, endA, startE, endE);
    }

    /* (non-Javadoc)
     * @see doc.snippets.SnippetExtractor#extractAnnotation()
     */
    @Override
    public String extractAnnotation(String line, String annotation) {
        //snippets are in the form
        String name = new String(line);
        name = name.replace("/", " ");
        name = name.replace(annotation, " ");
        name = name.trim();
        logger.debug("Extracted the " + name + " snippet from " + target);
        return name;
    }
}
