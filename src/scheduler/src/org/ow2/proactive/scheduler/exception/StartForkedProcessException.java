/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.exception;

import java.util.List;


/**
 * Used when starting forked java process.
 * If the process cannot be started or cannot be reached, this exception is raised. 
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class StartForkedProcessException extends StartProcessException {

    private List<String> command;

    /**
     * Create a new instance of StartForkedProcessException
     * 
     * @param msg the message describing the exception
     * @param command the forked java command that causes the exception
     */
    public StartForkedProcessException(String msg, List<String> command) {
        super(msg);
        if (command == null || command.size() < 1) {
            throw new IllegalArgumentException("Argument command must contain at least one element");
        }
        this.command = command;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLocalizedMessage() {
        StringBuilder sb = new StringBuilder(command.get(0));
        for (int i = 1; i < command.size(); i++) {
            sb.append(" " + command.get(i));
        }
        return super.getLocalizedMessage() + System.getProperty("line.separator") + "Command was : <" +
            sb.toString() + ">";
    }

    /**
     * Get the forked java command associated to this exception
     * 
     * @return the forked java command associated to this exception
     */
    public List<String> getCommand() {
        return this.command;
    }

}
