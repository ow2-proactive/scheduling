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
package org.objectweb.proactive.extra.gcmdeployment.GCMApplication;

import java.io.IOException;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMParserConstants;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNodeInternal;
import org.xml.sax.SAXException;


/**
 * A parser for the GCM Application descriptor schema.
 *
 * @author cmathieu
 *
 */
public interface GCMApplicationParser extends GCMParserConstants {

    /**
     * Returns all the Resources Providers
     * Descriptor
     *
     * @return all the declared Resources Providers as NodeProviderParams
     * @throws IOException
     * @throws SAXException
     */
    public Map<String, NodeProvider> getNodeProviders() throws SAXException, IOException;

    /**
     * Returns all the Virtual Node
     *
     * @return all the declared Virtual Nodes
     * @throws IOException
     * @throws SAXException
     */
    public Map<String, GCMVirtualNodeInternal> getVirtualNodes() throws SAXException, IOException;

    /**
     * Returns the Command Builder
     *
     * @return the Command Builder associated to this application
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    public CommandBuilder getCommandBuilder() throws XPathExpressionException, SAXException, IOException;
}
