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
package org.objectweb.proactive.extra.resourcemanager.nodesource.frontend;

import org.objectweb.proactive.extra.resourcemanager.frontend.RMAdmin;
import org.objectweb.proactive.extra.resourcemanager.nodesource.dynamic.DynamicNodeSource;


/**
 * Interface used by the {@link RMAdmin} frontend to manage
 * the {@link DynamicNodeSource} behaviour, threw three parameters :
 * <ul>
 * <li><b>NbMaxNodes</b> : the maximum number of nodes that we can have by
 *         this Node Source.</li>
 * <li><b>Time To Release (TTR)</b> : The time we want to keep the node before
 *         releasing it.</li>
 * <li><b>Nice Time</b> : The time before trying to get back a node after having released one.
 * </li>
 * </ul>
 * @author ProActive team
 *
 * /TODO gsigety, cdelbe: interface totally useless for the moment
 *
 */
public interface DynamicNSInterface {
    // GETTERS
    public int getNbMaxNodes();

    public int getTimeToRelease();

    public int getNiceTime();

    // SETTERS
    public void setNbMaxNodes(int nb);

    public void setTimeToRelease(int ttr);

    public void setNiceTime(int nice);
}
