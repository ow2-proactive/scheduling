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
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.xml.handler;

import org.xml.sax.SAXException;


/**
 * A handler for reading values from simple elements, such as
 * <pre><myElement>myValue</myElement></pre>
 *
 * @author Matthieu Morel
 *
 */
public class SingleValueUnmarshaller extends BasicUnmarshaller {

    /**
     * The implementation of this method ensures that even though the element value is split into several chunks,
     * we concatenate the chunks to build the actual value.
     * see http://www.saxproject.org/faq.html (The ContentHandler.characters() callback is missing data!)
     * and http://xml.apache.org/xerces2-j/faq-sax.html#faq-2
     * This method is called several times by {@link org.objectweb.proactive.core.xml.io.DefaultHandlerAdapter#characters(char[], int, int)}
     * if the data is split into several chunks.
     */
    @Override
    public void readValue(String value) throws SAXException {

        /*
        if (resultObject == null) {
            setResultObject(value);
        } else {
            setResultObject(resultObject + value);
        }
         */

        //Fix chunk reading problem
        if (resultObject != null) {
            value = resultObject + value;
        }

        //Transform variables into values if necessary
        if (org.objectweb.proactive.core.xml.VariableContractImpl.xmlproperties != null) {
            value = org.objectweb.proactive.core.xml.VariableContractImpl.xmlproperties.transform(value);
        }

        setResultObject(value);
    }
}
