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
package org.objectweb.proactive.extra.security.xacml;

import java.net.URI;

import org.objectweb.proactive.core.security.securityentity.Entity;

import com.sun.xacml.attr.AttributeValue;


public class EntityAttribute extends AttributeValue {

    /**
     * Official name of this type
     */
    public static final String identifier = "org.objectweb.proactive.core.security.securityentity.Entity"
            .replace('.', ':');

    /**
     * URI version of name for this type
     * <p>
     * This field is initialized by a static initializer so that we can catch
     * any exceptions thrown by URI(String) and transform them into a
     * RuntimeException, since this should never happen but should be reported
     * properly if it ever does.
     */
    private static URI identifierURI;

    /**
     * RuntimeException that wraps an Exception thrown during the creation of
     * identifierURI, null if none.
     */
    private static RuntimeException earlyException;

    static {
        try {
            identifierURI = new URI(identifier);
        } catch (Exception e) {
            earlyException = new IllegalArgumentException();
            earlyException.initCause(e);
        }
    };

    /**
     * 7 The actual String value that this object represents.
     */
    private Entity value;

    /**
     * Creates a new <code>StringAttribute</code> that represents the String
     * value supplied.
     *
     * @param value
     *            the <code>String</code> value to be represented
     */
    public EntityAttribute(Entity entity) {
        super(identifierURI);

        // Shouldn't happen, but just in case...
        if (earlyException != null) {
            throw earlyException;
        }

        this.value = entity;
    }

    // /**
    // * Returns a new <code>StringAttribute</code> that represents
    // * the xs:string at a particular DOM node.
    // *
    // * @param root the <code>Node</code> that contains the desired value
    // * @return a new <code>StringAttribute</code> representing the
    // * appropriate value (null if there is a parsing error)
    // */
    // public static EntityAttribute getInstance(Node root) {
    // Node node = root.getFirstChild();
    //
    //        
    // // Strings are allowed to have an empty AttributeValue element and are
    // // just treated as empty strings...we have to handle this case
    // if (node == null)
    // return new EntityAttribute(null);
    //
    // // get the type of the node
    // short type = node.getNodeType();
    //
    // // now see if we have (effectively) a simple string value
    // if ((type == Node.TEXT_NODE) || (type == Node.CDATA_SECTION_NODE) ||
    // (type == Node.COMMENT_NODE)) {
    // return getInstance(new Entity(node.getNodeValue());
    // }
    //
    // // there is some confusion in the specifications about what should
    // // happen at this point, but the strict reading of the XMLSchema
    // // specification suggests that this should be an error
    // return null;
    // }

    /**
     * Returns a new <code>StringAttribute</code> that represents the
     * xs:string value indicated by the <code>String</code> provided.
     *
     * @param value
     *            a string representing the desired value
     * @return a new <code>StringAttribute</code> representing the appropriate
     *         value
     */
    public static EntityAttribute getInstance(Entity entity) {
        return new EntityAttribute(entity);
    }

    /**
     * Returns the <code>String</code> value represented by this object.
     *
     * @return the <code>String</code> value
     */
    public Entity getValue() {
        return value;
    }

    /**
     * Returns true if the input is an instance of this class and if its value
     * equals the value contained in this class.
     *
     * @param o
     *            the object to compare
     *
     * @return true if this object and the input represent the same value
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EntityAttribute)) {
            return false;
        }

        EntityAttribute other = (EntityAttribute) o;

        return value.equals(other.value);
    }

    /**
     * Returns the hashcode value used to index and compare this object with
     * others of the same type. Typically this is the hashcode of the backing
     * data object.
     *
     * @return the object's hashcode value
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Converts to a String representation.
     *
     * @return the String representation
     */
    @Override
    public String toString() {
        return "EntityAttribute: \"" + value + "\"";
    }

    /**
     *
     */
    @Override
    public String encode() {
        return "<Entity type=\"" + value.getType() + "\" name=\"" + value.getName() + "\">";
    }
}
