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
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 * BooleanWrapper is the Scheduler Wrapper object for boolean.<br />
 * It is mostly used for Hibernate when using boolean with parametric type.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
@PublicAPI
@Entity
@Table(name = "BOOLEAN_WRAPPER")
@AccessType("field")
@Proxy(lazy = false)
public class BooleanWrapper implements Serializable {
    /**  */
	private static final long serialVersionUID = 21L;

	@Id
    @GeneratedValue
    @Column(name = "ID")
    @SuppressWarnings("unused")
    private long hId;

    @Column(name = "VALUE")
    private boolean value;

    /** HIBERNATE default constructor */
    @SuppressWarnings("unused")
    private BooleanWrapper() {
    }

    /**
     * Create a new instance of BooleanWrapper.
     *
     * @param value the boolean value of this wrapper.
     */
    public BooleanWrapper(boolean value) {
        this.value = value;
    }

    /**
     * Get the boolean value.
     *
     * @return the boolean value.
     */
    public boolean booleanValue() {
        return value;
    }

}
