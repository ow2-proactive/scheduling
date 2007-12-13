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
package org.objectweb.proactive.examples.jmx.remote.management.command.osgi;

import org.objectweb.proactive.examples.jmx.remote.management.command.CommandMBean;
import org.objectweb.proactive.examples.jmx.remote.management.mbean.BundleInfo;
import org.objectweb.proactive.examples.jmx.remote.management.osgi.OSGiStore;
import org.objectweb.proactive.examples.jmx.remote.management.status.Status;
import org.objectweb.proactive.examples.jmx.remote.management.transactions.Transaction;
import org.osgi.framework.Bundle;


public class StopCommand extends OSGiCommand implements StopCommandMBean {
    private long idBundle;

    public StopCommand(Transaction t, long id) {
        super(t, OSGiCommand.STOP + id, OSGiCommand.STOP);
        this.idBundle = id;
    }

    public Status undo_() {
        CommandMBean c = new StartCommand(this.transaction, this.idBundle);
        return c.do_();
    }

    public boolean check() {
        BundleInfo[] bundles = OSGiStore.getInstance().getBundles();
        for (int i = 0; i < bundles.length; i++) {
            if ((bundles[i].getId() == this.idBundle) &&
                ((bundles[i].getState() != Bundle.ACTIVE) && (bundles[i].getState() != Bundle.STARTING))) {
                this.done = true;
            }
        }
        return this.done;
    }
}
