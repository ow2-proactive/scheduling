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
package org.objectweb.proactive.examples.jmx.remote.management.osgi;

import java.util.HashMap;

import org.objectweb.proactive.examples.jmx.remote.management.mbean.BundleInfo;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.ungoverned.osgi.service.shell.ShellService;


public class OSGiStore {
    private static OSGiStore instance;
    private HashMap<String, BundleInfo> locationBundles = new HashMap<String, BundleInfo>();
    private BundleContext context;
    private ShellService shell;
    private String url;

    private OSGiStore() {
    }

    public static OSGiStore getInstance() {
        if (instance == null) {
            instance = new OSGiStore();
        }
        return instance;
    }

    /**
     * @return the context
     */
    public BundleContext getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(BundleContext context) {
        this.context = context;
    }

    /**
     * @return the shell
     */
    public ShellService getShell() {
        if (this.shell == null) {
            ServiceReference ref = this.context.getServiceReference(ShellService.class.getName());
            this.shell = (ShellService) this.context.getService(ref);
        }
        return this.shell;
    }

    public void registerBundle(String location, BundleInfo bInfo) {
        this.locationBundles.put(location, bInfo);
    }

    public synchronized BundleInfo[] getBundles() {
        BundleInfo[] ret = new BundleInfo[locationBundles.size()];
        this.locationBundles.values().toArray(ret);
        return ret;
    }

    public BundleInfo getBundleInfo(String location) {
        return this.locationBundles.get(location);
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    public String getBundleInfo(long idBundle) {
        Bundle b = this.context.getBundle(idBundle);
        return b.getLocation();
    }
}
