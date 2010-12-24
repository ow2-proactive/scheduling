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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui;

import java.util.HashMap;
import java.util.Map;


/**
 * Contains constants definitions and utility methods.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>
 */
public final class Internal {

    /**
     * Each icon image is represented by a unique ID that can be used to find the corresponding 
     * image from the registry.
     */
    public static final String IMG_CONNECT = "connect.gif";
    public static final String IMG_DISCONNECT = "disconnect.gif";
    public static final String IMG_FILEOBJ = "file_obj.gif";
    public static final String IMG_HORIZONTAL = "horizontal.gif";
    public static final String IMG_JOBKILL = "job_kill.gif";
    public static final String IMG_JOBOUTPUT = "job_output.gif";
    public static final String IMG_JOBPAUSERESUME = "job_pause_resume.gif";
    public static final String IMG_JOBPRIORITY = "job_priority.png";
    public static final String IMG_JOBSUBMIT = "job_submit.gif";
    public static final String IMG_MAXIMIZE = "maximize.gif";
    public static final String IMG_SCHEDULERFREEZE = "scheduler_freeze.gif";
    public static final String IMG_SCHEDULERKILL = "scheduler_kill.png";
    public static final String IMG_SCHEDULERPAUSE = "scheduler_pause.png";
    public static final String IMG_SCHEDULERRESUME = "scheduler_resume.png";
    public static final String IMG_SCHEDULERSHUTDOWN = "scheduler_shutdown.png";
    public static final String IMG_SCHEDULERSTART = "scheduler_start.png";
    public static final String IMG_SCHEDULERSTOP = "scheduler_stop.png";
    public static final String IMG_VERTICAL = "vertical.gif";
    public static final String IMG_REMOTE_CONNECTION = "visualization.png";
    public static final String IMG_SERVER = "server.png";
    public static final String IMG_SERVER_ADD = "server_add.png";
    public static final String IMG_SERVER_REMOVE = "server_remove.png";
    public static final String IMG_SERVER_STARTED = "server_started.png";
    public static final String IMG_SERVER_REBIND = "refresh.png";
    public static final String IMG_SERVER_STOPPED = "server_stopped.png";
    public static final String IMG_DATA = "data.png";
    public static final String IMG_EXIT = "exit.png";
    public static final String IMG_COPY = "copy.png";

    /**
     * associates a remote connection protocol with a binary on the local system
     */
    public static final Map<String, String> unixRemoteConnAssociation = new HashMap<String, String>();
    static {
        unixRemoteConnAssociation.put("vnc", "/usr/bin/vncviewer");
    }
    public static final Map<String, String> winRemoteConnAssociation = new HashMap<String, String>();
    static {
        winRemoteConnAssociation.put("vnc", "C:\\path\\to\\vncviewer");
    }
    public static final Map<String, String> macRemoteConnAssociation = new HashMap<String, String>();
    static {
        macRemoteConnAssociation.put("vnc", "/path/to/vncviewer");
    }

}