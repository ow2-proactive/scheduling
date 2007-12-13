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

import org.objectweb.proactive.examples.jmx.remote.management.mbean.BundleInfo;
import org.objectweb.proactive.examples.jmx.remote.management.osgi.OSGiStore;
import org.objectweb.proactive.examples.jmx.remote.management.status.Status;
import org.objectweb.proactive.examples.jmx.remote.management.transactions.Transaction;


public class UninstallCommand extends OSGiCommand implements UninstallCommandMBean {
    private long idBundle;

    public UninstallCommand(Transaction t, long id) {
        super(t, OSGiCommand.UNINSTALL + id, OSGiCommand.UNINSTALL);
        this.idBundle = id;
    }

    public Status undo_() {
        String location = OSGiStore.getInstance().getBundleInfo(this.idBundle);
        InstallCommand c = new InstallCommand(this.transaction, location, OSGiCommand.INSTALL);
        return c.do_();
    }

    public boolean check() {
        BundleInfo[] bundles = OSGiStore.getInstance().getBundles();
        for (int i = 0; i < bundles.length; i++) {
            if (bundles[i].getId() == this.idBundle) {
                this.done = false;
                return this.done;
            }
        }
        this.done = true;
        return true;
    }
}
