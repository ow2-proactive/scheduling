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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package testsuite.xml;

import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;

import testsuite.manager.AbstractManager;
import testsuite.manager.AbstractManagerConstants;


/**
 * @author Alexandre di Costanzo
 *
 */
public class ResultHandler extends BasicUnmarshaller
    implements ManagerDescriptorConstants {
    private AbstractManager manager = null;

    ResultHandler(AbstractManager manager) {
        super();
        this.manager = manager;
    }

    @Override
	public void startContextElement(String name, Attributes attributes)
        throws org.xml.sax.SAXException {
        String type = (String) attributes.getValue("type");
        String file = (String) attributes.getValue("file");
        if (type.equalsIgnoreCase("html")) {
            this.manager.setResultType(AbstractManagerConstants.HTML);
            this.manager.setOutputPath(file);
        } else if (type.equalsIgnoreCase("text")) {
            this.manager.setResultType(AbstractManagerConstants.TEXT);
            this.manager.setOutputPath(file);
        } else if (type.equalsIgnoreCase("console")) {
            this.manager.setResultType(AbstractManagerConstants.CONSOLE);
        } else if (type.equalsIgnoreCase("xml")) {
            this.manager.setResultType(AbstractManagerConstants.XML);
            this.manager.setOutputPath(file);
        }
    }

    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
    }
}
