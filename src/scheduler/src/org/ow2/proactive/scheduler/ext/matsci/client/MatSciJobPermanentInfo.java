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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.matsci.client;

import java.util.ArrayList;
import java.util.TreeSet;

import org.ow2.proactive.scheduler.ext.matsci.common.PASolveMatSciGlobalConfig;


/**
 * MatSciJobPermanentInfo
 *
 * @author The ProActive Team
 */
public class MatSciJobPermanentInfo implements java.io.Serializable, Cloneable {

    String jobId;

    TreeSet<String> tnames;
    TreeSet<String> finaltnames;

    int nbres;

    int depth;

    PASolveMatSciGlobalConfig conf;

    public String getJobId() {
        return jobId;
    }

    public int getNbres() {
        return nbres;
    }

    public int getDepth() {
        return depth;
    }

    public PASolveMatSciGlobalConfig getConf() {
        return conf;
    }

    public MatSciJobPermanentInfo(String jobId, int nbres, int depth, PASolveMatSciGlobalConfig conf,
            TreeSet<String> tnames, TreeSet<String> finaltnames) {
        this.jobId = jobId;
        this.nbres = nbres;
        this.depth = depth;
        this.conf = conf;
        this.tnames = tnames;
        this.finaltnames = finaltnames;

    }

    public TreeSet<String> getTaskNames() {
        return tnames;
    }

    public TreeSet<String> getFinalTaskNames() {
        return finaltnames;
    }

    public ArrayList<String> getFinalTasksNamesAsList() {
        ArrayList<String> lst = new ArrayList<String>();
        lst.addAll(finaltnames);
        return lst;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        MatSciJobPermanentInfo newinfo = (MatSciJobPermanentInfo) super.clone();
        newinfo.tnames = (TreeSet<String>) tnames.clone();
        newinfo.finaltnames = (TreeSet<String>) finaltnames.clone();
        return newinfo;
    }
}
