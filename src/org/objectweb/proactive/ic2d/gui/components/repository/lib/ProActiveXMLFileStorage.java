/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.ic2d.gui.components.repository.lib;

import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.xml.XMLWriter;
import org.objectweb.fractal.gui.repository.api.Storage;
import org.objectweb.fractal.gui.repository.lib.XMLFileStorage;

import java.io.File;
import java.io.FileWriter;


/**
 * Basic implementation of {@link Storage} interface, based on a single XML
 * file. More precsiely, each storage is a single XML file, and each (name,
 * value) pair is represented by an XML element of these files (the name is
 * given by the 'name' attribute of an XML element).
 *
 * @author Matthieu Morel
 */
public class ProActiveXMLFileStorage extends XMLFileStorage {
    public void store(final String name, final Object value)
        throws Exception {
        if (storage == null) {
            throw new Exception("Storage not opened");
        }
        String n = name.replace('.', '/') + ".fractal";
        File f = new File(storage, n);
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        FileWriter pw = new FileWriter(f);
        pw.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n");
        //pw.write("<!DOCTYPE definition PUBLIC \"-//objectweb.org//DTD Fractal ADL 2.0//EN\" \"classpath://org/objectweb/fractal/adl/xml/standard.dtd\">\n\n");
        pw.write(
            "<!DOCTYPE definition PUBLIC \"-//objectweb.org//DTD Fractal ADL 2.0//EN\" \"classpath://org/objectweb/proactive/core/component/adl/xml/proactive.dtd\">\n\n");
        XMLWriter xmlw = new XMLWriter(pw);
        xmlw.write((Node) value);
        pw.close();
    }

    public void close() throws Exception {
        if (storage == null) {
            throw new Exception("Storage not opened");
        }
        storage = null;
    }
}
