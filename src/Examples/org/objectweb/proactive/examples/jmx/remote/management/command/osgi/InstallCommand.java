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

import java.io.Serializable;

import org.objectweb.proactive.examples.jmx.remote.management.command.CommandMBean;
import org.objectweb.proactive.examples.jmx.remote.management.mbean.BundleInfo;
import org.objectweb.proactive.examples.jmx.remote.management.osgi.OSGiStore;
import org.objectweb.proactive.examples.jmx.remote.management.status.Status;
import org.objectweb.proactive.examples.jmx.remote.management.transactions.Transaction;


public class InstallCommand extends OSGiCommand implements InstallCommandMBean, Serializable {

    /**
     *
     */
    private String location;

    public InstallCommand(Transaction t, String location, String type) {
        super(t, OSGiCommand.INSTALL + location, type);
        this.location = location;
    }

    public Status undo_() {
        BundleInfo bInfo = OSGiStore.getInstance().getBundleInfo(this.location);
        if (bInfo != null) {
            CommandMBean c = new UninstallCommand(this.transaction, bInfo.getId());
            return c.do_();
        }
        return new Status(Status.ERR, OSGiCommand.UNINSTALL, "Bundle does not exist", OSGiStore.getInstance()
                .getUrl());
    }

    public boolean check() {
        BundleInfo[] bundles = OSGiStore.getInstance().getBundles();
        for (int i = 0; i < bundles.length; i++) {
            if (bundles[i].getLocation().equals(this.location)) {
                this.done = true;
            }
        }
        return this.done;
    }
}
