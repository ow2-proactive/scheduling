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
package org.objectweb.proactive.extra.gcmdeployment;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class GCMDeploymentLoggers {
    static final public String GCM_DEPLOYMENT = Loggers.DEPLOYMENT + ".GCMD";
    static final public String GCM_APPLICATION = Loggers.DEPLOYMENT + ".GCMA";
    static final public String GCM_NODEALLOC = Loggers.DEPLOYMENT + ".nodeAllocator";
    static final public Logger GCMD_LOGGER = ProActiveLogger.getLogger(GCM_DEPLOYMENT);
    static final public Logger GCMA_LOGGER = ProActiveLogger.getLogger(GCM_APPLICATION);
    static final public Logger GCM_NODEALLOC_LOGGER = ProActiveLogger.getLogger(GCM_NODEALLOC);
}
