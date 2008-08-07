/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */

/**
 * Result is return to the GUI.
 * It will be treat in order to display proper Error or Warning message to the user.
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class Result {
	private boolean success;
	private String msg;
	private boolean force = false;
	public Result(boolean success, String msg){
		this.success = success;
		this.msg = msg;
	}
	public Result(boolean success, String msg, boolean force){
		this.success = success;
		this.msg = msg;
		this.force = force;
	}
	public String getMsg() {
		return msg;
	}
	public boolean isSuccess() {
		return success;
	}
	public boolean isForce() {
		return force;
	}
}
