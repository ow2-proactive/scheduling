/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.osgi;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;

import java.io.IOException;


/**
 * @see org.objectweb.proactive.osgi.ProActiveService
 * @author vlegrand
 *
 */
public class ProActiveServicesImpl implements ProActiveService {
    private Node node;

    public ProActiveServicesImpl(Node node) {
        this.node = node;
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#newActive(java.lang.String, java.lang.Object[])
     */
    public Object newActive(String className, Object[] params)
        throws ActiveObjectCreationException, NodeException {
        return ProActive.newActive(className, params, this.node);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#register(java.lang.Object, java.lang.String)
     */
    public void register(Object obj, String url) throws IOException {
        ProActive.register(obj, url);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#unregister(java.lang.String)
     */
    public void unregister(String url) throws IOException {
        ProActive.unregister(url);
    }
}
