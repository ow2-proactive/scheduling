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
package org.objectweb.proactive.examples.jmx.remote.management.mbean;

import java.util.ArrayList;

import org.objectweb.proactive.examples.jmx.remote.management.status.Status;
import org.objectweb.proactive.examples.jmx.remote.management.transactions.Transaction;


/**
 * @author vlegrand
 *
 */
public interface OsgiMBean {

    /**
     *
     * @return
     */
    public ArrayList getBundles();

    /**
     *
     * @return
     */
    public String getBundleStringList();

    /**
     *
     * @return
     */
    public String getVersion();

    /**
     *
     * @return
     */
    public String getVendor();

    /**
     *
     * @return
     */
    public String getLanguage();

    /**
     *
     * @return
     */
    public String getOsName();

    /**
     *
     * @return
     */
    public String getProcessor();

    /**
     *
     * @return
     */
    public String getExeEnv();

    /**
     *
     * @return
     */
    public String getProfile();

    /**
     *
     * @return
     */
    public String getUrl();

    /**
     *
     * @param key
     * @return
     */
    public String getProperty(String key);

    //actions       

    /**
     *
     */
    public Status installBundle(long id, String url);

    /**
     *
     * @param id
     * @return
     */
    public Status stopBundle(long idTransacton, long id);

    /**
     *
     * @param location
     * @return
     */
    public Status stopBundle(long id, String location);

    /**
     *
     * @param id
     * @return
     */
    public Status startBundle(long idTransaction, long id);

    /**
     *
     * @param location
     * @return
     */
    public Status startBundle(long id, String location);

    /**
     *
     * @param id
     * @return
     */
    public Status updateBundle(long idTransaction, long id);

    /**
     *
     * @param location
     * @return
     */
    public Status updateBundle(long id, String location);

    /**
     *
     * @param id
     * @return
     */
    public Status uninstallBundle(long idTransaction, long id);

    /**
     *
     * @param location
     * @return
     */
    public Status uninstallBundle(long id, String location);

    /**
     *
     * @param cmd
     * @return
     */
    public Status executeCommand(long id, String cmd);

    /**
     *
     * @param cmds
     * @return
     */
    public Status executePlan(long id, String[] cmds);

    /**
     *
     * @return
     */
    public Transaction beginTransaction();

    /**
     *
     * @return
     */
    public Status commitTransaction(long id);

    /**
     *
     * @return
     */
    public Status rollbackTransaction(long id);

    public boolean getSecure();
}
