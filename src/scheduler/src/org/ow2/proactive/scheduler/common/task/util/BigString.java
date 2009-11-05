/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task.util;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 * BigString is the Scheduler Wrapper object for String.<br />
 * It is mostly used for Hibernate when using string as value in a hashMap.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
@PublicAPI
@Entity
@Table(name = "BIG_STRING")
@AccessType("field")
@Proxy(lazy = false)
public final class BigString implements Serializable {
    /**  */
    private static final long serialVersionUID = 20;

    @Id
    @GeneratedValue
    @Column(name = "ID")
    @SuppressWarnings("unused")
    private long hId;

    @Column(name = "VALUE", length = Integer.MAX_VALUE)
    @Lob
    private String value;

    /** Hibernate default constructor */
    public BigString() {
    }

    /**
     * Create a new instance of BigString.
     *
     * @param value
     */
    public BigString(String value) {
        this.value = value;
    }

    /**
     * Get the String value.
     *
     * @return the String value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the String value value to the given value value.
     *
     * @param value the String value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

}
