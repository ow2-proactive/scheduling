package org.objectweb.proactive.extensions.masterworker.core;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.SubMaster;
import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.MasterIntern;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.ResultIntern;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * The SubMasterImpl class is acting as a client on the worker side to talk to the master, 
 * submit tasks, wait results, etc... <br>
 *
 * @author The ProActive Team
 */
public class SubMasterImpl implements SubMaster<Task<Serializable>, Serializable>, Serializable {

    private final MasterIntern master;
    private final String originatorName;

    public SubMasterImpl(MasterIntern master, String originatorName) {
        this.master = master;
        this.originatorName = originatorName;
    }

    public void setResultReceptionOrder(OrderingMode mode) {
        master.setResultReceptionOrder(originatorName, mode);
    }

    /**
    * {@inheritDoc}
    */
    public void solve(List<Task<Serializable>> tasks) {
        master.solveIntern(originatorName, tasks);
    }

    /**
    * {@inheritDoc}
    */
    @SuppressWarnings("unchecked")
    public List<Serializable> waitAllResults() throws TaskException {
        if (master.isEmpty(originatorName)) {
            throw new IllegalStateException("Master is empty, call to this method will wait forever");
        }
        List<ResultIntern<Serializable>> completed = (List<ResultIntern<Serializable>>) PAFuture
                .getFutureValue(master.waitAllResults(originatorName));
        List<Serializable> results = new ArrayList<Serializable>();
        for (ResultIntern<Serializable> res : completed) {
            if (res.threwException()) {
                throw new TaskException(res.getException());
            }

            Serializable obj = res.getResult();
            if (obj != null) {
                results.add(obj);
            } else {
                results.add(null);
            }

        }

        return results;
    }

    /**
    * {@inheritDoc}
    */
    @SuppressWarnings("unchecked")
    public Serializable waitOneResult() throws TaskException {
        if (master.isEmpty(originatorName)) {
            throw new IllegalStateException("Master is empty, call to this method will wait forever");
        }
        ResultIntern<Serializable> completed = (ResultIntern<Serializable>) PAFuture.getFutureValue(master
                .waitOneResult(originatorName));
        if (completed.threwException()) {
            throw new TaskException(completed.getException());
        }

        Serializable obj = completed.getResult();
        if (obj != null) {
            return obj;
        }

        return null;
    }

    /**
    * {@inheritDoc}
    */
    @SuppressWarnings("unchecked")
    public List<Serializable> waitKResults(int k) throws TaskException {
        if (master.countPending(originatorName) < k) {
            throw new IllegalStateException("Number of tasks submitted previously is strictly less than " +
                k + ": call to this method will wait forever");
        }
        List<ResultIntern<Serializable>> completed = (List<ResultIntern<Serializable>>) PAFuture
                .getFutureValue(master.waitKResults(originatorName, k));
        List<Serializable> results = new ArrayList<Serializable>();
        for (ResultIntern<Serializable> res : completed) {
            if (res.threwException()) {
                throw new TaskException(res.getException());
            }

            Serializable obj = res.getResult();
            if (obj != null) {
                results.add(obj);
            } else {
                results.add(null);
            }

        }

        return results;
    }

    /**
    * {@inheritDoc}
    */
    @SuppressWarnings("unchecked")
    public boolean isEmpty() {
        return master.isEmpty(originatorName);
    }

    /**
    * {@inheritDoc}
    */
    @SuppressWarnings("unchecked")
    public int countAvailableResults() {
        return master.countAvailableResults(originatorName);
    }
}
