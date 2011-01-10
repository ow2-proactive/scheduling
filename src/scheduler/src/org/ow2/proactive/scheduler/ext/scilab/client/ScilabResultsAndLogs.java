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
package org.ow2.proactive.scheduler.ext.scilab.client;

import org.ow2.proactive.scheduler.ext.matsci.client.MatSciTaskStatus;
import org.ow2.proactive.scheduler.ext.matsci.client.ResultsAndLogs;
import org.scilab.modules.types.ScilabType;


/**
 * ResultsAndLogs wrapper of scilab tasks results
 *
 * @author The ProActive Team
 */
public class ScilabResultsAndLogs extends ResultsAndLogs<ScilabType> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 30L;

	public ScilabResultsAndLogs() {
        super();
    }

    public ScilabResultsAndLogs(ScilabType result, String logs, Throwable exception, MatSciTaskStatus status) {
        super(result, logs, exception, status);
    }

}
