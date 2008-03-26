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
package org.objectweb.proactive.core.xml.io;

import java.io.IOException;


/**
 *
 * Implement an XLMReader based on a existing DOM. We assume that the node given in parameter to the
 * constructor is the context of the tree to read.
 *
 * @author The ProActive Team
 * @version      0.91
 *
 */
public class DOMReader implements XMLReader {
    private org.w3c.dom.Element rootElement;
    private DOMAdaptor domAdaptor;

    public DOMReader(org.w3c.dom.Element rootElement, XMLHandler xmlHandler) {
        this.rootElement = rootElement;
        this.domAdaptor = new DOMAdaptor(xmlHandler);
    }

    // -- implements XMLReader ------------------------------------------------------
    public void read() throws IOException {
        domAdaptor.read(rootElement);
    }
}
