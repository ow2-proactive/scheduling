package org.ow2.proactive.scheduler.ext.matsci.client;

import org.ow2.proactive.scheduler.ext.matsci.common.PASolveMatSciGlobalConfig;

import java.util.TreeSet;


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

    @Override
    public Object clone() throws CloneNotSupportedException {
        MatSciJobPermanentInfo newinfo = (MatSciJobPermanentInfo) super.clone();
        newinfo.tnames = (TreeSet<String>) tnames.clone();
        newinfo.finaltnames = (TreeSet<String>) finaltnames.clone();
        return newinfo;
    }
}
