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
package org.objectweb.proactive.extensions.scheduler.common.scheduler;

import java.io.Serializable;
import java.util.HashMap;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * It will be used to view some tips on the scheduler.<br>
 * You can find inside, a map of every instances on which statistics can be done.
 *
 *
 * @author The ProActive Team
 * @version 3.9, Jul 25, 2007
 * @since ProActive 3.9
 */
@PublicAPI
public interface Stats extends Serializable {

    /**
     * To get the properties saved in the stats class as an hashMap
     *
     * @return the properties as an hashMap.
     */
    public HashMap<String, Object> getProperties();
}
