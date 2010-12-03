/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.common.task;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Proxy;


/**
 * PropertyModifier is used to have an history of modification to apply to properties.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
@Entity
@Table(name = "PROPERTY_MODIFIER")
@AccessType("field")
@Proxy(lazy = false)
@XmlAccessorType(XmlAccessType.FIELD)
public final class PropertyModifier implements Serializable {

    @Id
    @GeneratedValue
    @XmlTransient
    private long hId;

    private String name = null;

    @Column(name = "VALUE", length = Integer.MAX_VALUE)
    @Lob
    private String value = null;

    private boolean append = false;

    private char appendChar = 0;

    /**
     * Hibernate empty constructor
     */
    public PropertyModifier() {
    }

    /**
     * Create a new instance of PropertyModifier
     *
     * @param name
     * @param value
     * @param append
     */
    public PropertyModifier(String name, String value, boolean append) {
        this.name = name;
        this.value = value;
        this.append = append;
    }

    /**
     * Create a new instance of PropertyModifier
     *
     * @param name
     * @param value
     * @param appendChar
     */
    public PropertyModifier(String name, String value, char appendChar) {
        this(name, value, true);
        this.appendChar = appendChar;
    }

    /**
     * Update the given StringBuilder with this property.<br/>
     * Create, overwrite or append the value in the given stringBuilder as it is describe in this property.
     *
     * @param sb the stringBuilder to be updated, cannot be null
     * @throws IllegalArgumentException if sb is null
     */
    public void update(StringBuilder sb) {
        if (sb == null) {
            throw new IllegalArgumentException("Given string builder cannot be null");
        }
        if (append || appendChar != 0) {
            if (appendChar != 0 && sb.length() > 0) {
                sb.append(appendChar);
            }
            sb.append(value);
        } else {
            sb.delete(0, sb.length());
            sb.append(value);
        }
    }

    /**
     * Update the given map with this property.<br/>
     * Put or update the entry denoted by this property name.
     * Create, overwrite or append the value as it is describe in this property.
     *
     * @param props the map in which to add the property, cannot be null
     * @throws IllegalArgumentException if sb is null
     */
    public void update(Map<String, String> props) {
        if (props == null) {
            throw new IllegalArgumentException("Given map cannot be null");
        }
        if (props.get(name) != null && (append || appendChar != 0)) {
            if (appendChar != 0 && props.get(name).length() > 0) {
                props.put(name, props.get(name) + appendChar);
            }
            props.put(name, props.get(name) + value);
        } else {
            props.put(name, value);
        }
    }

    /**
     * Get the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the value
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Get the append
     *
     * @return the append
     */
    public boolean isAppend() {
        return append;
    }

    /**
     * Get the appendChar
     *
     * @return the appendChar
     */
    public char getAppendChar() {
        return appendChar;
    }

}
