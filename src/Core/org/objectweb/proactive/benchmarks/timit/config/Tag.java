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
package org.objectweb.proactive.benchmarks.timit.config;

import java.util.HashMap;
import java.util.Iterator;

import org.jdom.Attribute;
import org.jdom.Element;


/**
 * Read all attribute of a given Element and put them in a hashmap, then it can
 * be used in an easier way.
 *
 * @author The ProActive Team
 *
 */
public abstract class Tag {
    private HashMap<String, String> attributes; // <variable name,value>

    /**
     * Construct a tag without initial values
     */
    public Tag() {
        this.attributes = new HashMap<String, String>();
    }

    /**
     * Construct a Tag from all attributes of a given Element
     *
     * @param eBench
     *            the Element to read
     */
    public Tag(Element eBench) {
        this();
        Iterator it = eBench.getAttributes().iterator();
        while (it.hasNext()) {
            Attribute attr = (Attribute) it.next();
            this.addAttribute(attr.getName(), attr.getValue());
        }
    }

    /**
     * Extend current tag with new attributes values
     *
     * @param name
     * @param value
     */
    public void addAttribute(String name, String value) {
        this.attributes.put(name.toLowerCase(), value);
    }

    /**
     * @return the value of the given attribute
     */
    public String get(String name) {
        return this.attributes.get(name.toLowerCase());
    }

    /**
     * Construct a pretty printed tag. For debug purpose
     */
    @Override
    public String toString() {
        String result = "[";
        Iterator<String> it = this.attributes.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            String value = this.attributes.get(key);
            result += (key + "=" + value + (it.hasNext() ? ", " : ""));
        }
        return result + "]\n";
    }
}
