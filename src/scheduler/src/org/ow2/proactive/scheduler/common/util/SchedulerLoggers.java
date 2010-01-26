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
package org.ow2.proactive.scheduler.common.util;

/**
 * SchedulerLoggers is used for administrator and user loggers only.
 * See SchedulerDevLoggers developer loggers.
 *
 * @author The ProActive Team
 */
public interface SchedulerLoggers {

    static final public String PREFIX = "proactive.scheduler.dev";

    static final public String SCHEDULER = PREFIX + ".admin";

    static final public String CONSOLE = SCHEDULER + ".consol";

    static final public String CORE = SCHEDULER + ".core";

    static final public String DATABASE = SCHEDULER + ".database";

    static final public String FRONTEND = SCHEDULER + ".frontend";

    static final public String CONNECTION = SCHEDULER + ".connection";

    static final public String FACTORY = SCHEDULER + ".factory";

    static final public String MATLAB = SCHEDULER + ".matlab";

    static final public String SCILAB = SCHEDULER + ".scilab";

    static final public String UTIL = SCHEDULER + ".util";

    static final public String RMPROXY = SCHEDULER + ".RMproxy";

}
