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
package org.objectweb.proactive.core.util.log;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;


/**
 * @author Arnaud Contes
 *
 *  This class stores all logger used in proactive. It provides an easy way
 *  to create and to retrieve a logger.
 */
public class ProActiveLogger extends Logger {
    // It's enough to instantiate a factory once and for all.
    private static ProActiveLoggerFactory myFactory = new ProActiveLoggerFactory();

    /**
       Just calls the parent constuctor.
     */
    protected ProActiveLogger(String name) {
        super(name);
    }

    /**
       This method overrides {@link Logger#getInstance} by supplying
       its own factory type as a parameter.
     */
    public static Category getInstance(String name) {
        return Logger.getLogger(name, myFactory);
    }

    /**
       This method overrides {@link Logger#getLogger} by supplying
       its own factory type as a parameter.
     */
    public static Logger getLogger(String name) {
        return Logger.getLogger(name, myFactory);
    }
}
