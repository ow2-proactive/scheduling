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
package org.objectweb.proactive.core.descriptor.services;

import java.util.Map;


public class TechnicalServiceXmlType {
    private String id;
    private Class<?> type;
    private Map args;

    public TechnicalServiceXmlType() {
    }

    public TechnicalServiceXmlType(String id, Class<?> type, Map args) {
        this.id = id;
        this.type = type;
        this.args = args;
    }

    /**
    * @return Returns the args.
    */
    public Map getArgs() {
        return args;
    }

    /**
     * @param args The args to set.
     */
    public void setArgs(Map args) {
        this.args = args;
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The id to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return Returns the type.
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(Class<?> type) {
        this.type = type;
    }
}
