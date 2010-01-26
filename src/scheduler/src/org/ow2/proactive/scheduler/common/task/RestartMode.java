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
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.common.task;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 * This class represents the different restart mode for a task if an error occurred during its execution.<br>
 * !! It is no longer an Enum since the rise of Hibernate.
 *
 * @author The ProActive Team
 * @since ProActive 4.0
 */
@PublicAPI
@Entity
@Table(name = "RESTART_MODE")
@AccessType("field")
@Proxy(lazy = false)
public class RestartMode implements java.io.Serializable {

    // WARNING, DO NOT CHANGE index property in RestartMode construction,
    // 1="Anywhere", 2="Elsewhere" (Changing the index can create inconsistent state in DB)

    /**  */
    private static final long serialVersionUID = 200;
    /**
     * The task will be restarted according to its possible resources.
     */
    public static final RestartMode ANYWHERE = new RestartMode(1, "Anywhere");
    /**
     * The task will be restarted on an other node.
     */
    public static final RestartMode ELSEWHERE = new RestartMode(2, "Elsewhere");

    @Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long hId;

    @Column(name = "INDEX_")
    private int index;

    @Column(name = "DESCRIPTION")
    private String description;

    /** HIBERNATE default constructor */
    private RestartMode() {
    }

    /**
     * Implicit constructor of a restart mode.
     *
     * @param description the name of the restart mode.
     */
    private RestartMode(int index, String description) {
        this.index = index;
        this.description = description;
    }

    /**
     * Return the RestartMode  corresponding to the given sMode String.
     *
     * @param description a string representing the restart mode.
     * @return the RestartMode.
     */
    public static RestartMode getMode(String description) {
        if (ELSEWHERE.description.equalsIgnoreCase(description)) {
            return ELSEWHERE;
        } else {
            return ANYWHERE;
        }
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return description;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        try {
            return index == ((RestartMode) obj).index;
        } catch (ClassCastException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
    }

}
