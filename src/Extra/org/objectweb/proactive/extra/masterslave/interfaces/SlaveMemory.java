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
package org.objectweb.proactive.extra.masterslave.interfaces;


/**
 * This interface gives access to the memory of a slave, a task can record data in this memory under a specific name. <br/>
 * This data could be loaded later on by another task <br/>
 * @author fviale
 *
 */
public interface SlaveMemory {

    /**
     * Save data under a specific name
     * @param name name of the data
     * @param data data to be saved
     */
    void save(String name, Object data);

    /**
     * Load some data previously saved
     * @param name the name under which the data was saved
     * @return the data
     */
    Object load(String name);

    /**
     * Erase some data previously saved
     * @param name the name of the data which need to be erased
     */
    void erase(String name);
}
