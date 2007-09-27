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
package functionalTests.runtime.classloader;

import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;


/**
 * @author Matthieu Morel
 *
 */
public class B {
    public B() {
    }

    public B(String param) {
        try {
            createActiveObjectC();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ProActiveRuntimeException(e);
        }
    }

    public void createActiveObjectC() throws Exception {
        ProActiveDescriptor descriptor = ProDeployment.getProactiveDescriptor(getClass()
                                                                                  .getResource("/deployment-tmp.xml")
                                                                                  .getPath());
        descriptor.activateMappings();
        Object ao = ProActiveObject.newActive("functionalTests.runtime.classloader.C",
                new Object[] { "sdfasdf" },
                descriptor.getVirtualNode("VN1").getNode());
        descriptor.killall(false);
    }
}
