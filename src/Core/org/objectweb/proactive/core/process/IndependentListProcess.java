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
package org.objectweb.proactive.core.process;

import java.io.IOException;
import java.util.ArrayList;


/**
 * This class contains a list of processes which have to be executed sequentially
 *
 * @author ProActiveTeam
 * @version 1.0, 01 Dec 2005
 * @since ProActive 3.0
 *
 */
public class IndependentListProcess
    extends AbstractSequentialListProcessDecorator {
    public IndependentListProcess() {
        super();
    }

    public IndependentListProcess(ArrayList processes) {
        super();
        this.processes = processes;
    }

    @Override
    protected ExternalProcess createProcess() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getHostname() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setHostname(String hostname) {
        // TODO Auto-generated method stub
    }

    public boolean isHierarchical() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isDependent() {
        // TODO Auto-generated method stub
        return false;
    }

    public void startProcess() throws IOException {
        // TODO Auto-generated method stub
    }
}
