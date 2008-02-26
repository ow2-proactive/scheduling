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
package org.objectweb.proactive.extensions.masterworker;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.masterworker.core.AOMaster;
import org.objectweb.proactive.extensions.masterworker.core.AOTaskRepository;
import org.objectweb.proactive.extensions.masterworker.interfaces.Master;
import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.ResultIntern;


/**
 * Entry point of the Master/Worker API.<br/>
 * Here is how the Master/Worker API is used :
 * <ol>
 * <li>Create a ProActiveMaster object through the different constructors</li>
 * <li>Submit tasks through the use of the <b><i>solve</i></b> methods</li>
 * <li>Collect results through the <b><i>wait</i></b> methods</li>
 * </ol>
 * <br/>
 * The <b><i>WorkerMemory</i></b> concept is meant to allow user to store information directly inside the workers where tasks are executed. <br/>
 * The WorkerMemory has the same structure as a Dictionary with &lt;key, value&gt; pairs where keys are string and values are any Java object. <br/>
 * <br/>
 * A user can specify, when creating the master, the initial memory that every worker will have by providing a Map of &lt;String,Object&gt; pairs to the ProActiveMaster constructors.<br/>
 * <br/>
 * When tasks will later on be executed on the workers, the tasks will be able to access this memory through the worker memory parameter of the <b><i>run</i></b> method.
 * <br/>
 * The results can be received using two different reception order modes: <br/>
 * <ul>
 * <li>In the <b><i>CompletionOrder mode</i></b>, which is the default, results are received in the same order as they are completed by the workers (i.e. order is unspecified).</li>
 * <li>In the <b><i>SubmissionOrder mode</i></b>, results are received in the same order as they are submitted to the master.</li>
 * </ul>
 * <br/>
 *
 * @see org.objectweb.proactive.extensions.masterworker.interfaces.Task
 * @see org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory
 * @see org.objectweb.proactive.extensions.masterworker.interfaces.Master
 *
 *
 * @author fviale
 *
 * @param <T> Task of result R
 * @param <R> Result Object
 */
@PublicAPI
public class ProActiveMaster<T extends Task<R>, R extends Serializable> implements Master<T, R>, Serializable {

    /**
     *
     */
    protected AOMaster aomaster = null;
    protected AOTaskRepository aorepository = null;

    /**
     * Creates a local master (you can add resources afterwards)
     */
    public ProActiveMaster() {
        this(new HashMap<String, Object>());
    }

    /**
     * Creates a remote master that will be created on top of the given Node <br>
     * Resources can be added to the master afterwards
     * @param remoteNodeToUse this Node will be used to create the remote master
     */
    public ProActiveMaster(Node remoteNodeToUse) {
        this(remoteNodeToUse, new HashMap<String, Object>());
    }

    /**
     * Creates an empty remote master that will be created on top of the given Node with an initial worker memory
     * @param remoteNodeToUse this Node will be used to create the remote master
     * @param initialMemory initial memory that every workers deployed by the master will have
     */
    public ProActiveMaster(Node remoteNodeToUse, Map<String, Object> initialMemory) {
        try {
            aorepository = (AOTaskRepository) PAActiveObject.newActive(AOTaskRepository.class.getName(),
                    new Object[] {}, remoteNodeToUse);

            aomaster = (AOMaster) PAActiveObject.newActive(AOMaster.class.getName(), new Object[] {
                    aorepository, initialMemory }, remoteNodeToUse);
        } catch (ActiveObjectCreationException e) {
            throw new IllegalArgumentException(e);
        } catch (NodeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Creates an empty local master with an initial worker memory
     * @param initialMemory initial memory that every workers deployed by the master will have
     */
    public ProActiveMaster(Map<String, Object> initialMemory) {
        try {
            aorepository = (AOTaskRepository) PAActiveObject.newActive(AOTaskRepository.class.getName(),
                    new Object[] {});

            aomaster = (AOMaster) PAActiveObject.newActive(AOMaster.class.getName(), new Object[] {
                    aorepository, initialMemory });
        } catch (ActiveObjectCreationException e) {
            throw new IllegalArgumentException(e);
        } catch (NodeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Creates an empty remote master with the URL of a descriptor and the name of a virtual node
     * The master will be created on top of a single resource deployed by this virtual node
     * @param descriptorURL url of the ProActive descriptor
     * @param masterVNName name of the virtual node to deploy inside the ProActive descriptor
     */
    public ProActiveMaster(URL descriptorURL, String masterVNName) {
        this(descriptorURL, masterVNName, new HashMap<String, Object>());
    }

    /**
     * Creates an empty remote master with the URL of a descriptor and the name of a virtual node
     * The master will be created on top of a single resource deployed by this virtual node
     * @param descriptorURL url of the ProActive descriptor
     * @param masterVNName name of the virtual node to deploy inside the ProActive descriptor
     * @param initialMemory initial memory that every workers deployed by the master will have
     */
    public ProActiveMaster(URL descriptorURL, String masterVNName, Map<String, Object> initialMemory) {
        try {
            ProActiveDescriptor pad = PADeployment.getProactiveDescriptor(descriptorURL.toExternalForm());
            VirtualNode masterVN = pad.getVirtualNode(masterVNName);
            masterVN.activate();

            Node masterNode = masterVN.getNode();
            aorepository = (AOTaskRepository) PAActiveObject.newActive(AOTaskRepository.class.getName(),
                    new Object[] {}, masterNode);

            aomaster = (AOMaster) PAActiveObject.newActive(AOMaster.class.getName(), new Object[] {
                    aorepository, initialMemory }, masterNode);
        } catch (ActiveObjectCreationException e) {
            throw new IllegalArgumentException(e);
        } catch (NodeException e) {
            throw new IllegalArgumentException(e);
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addResources(Collection<Node> nodes) {
        aomaster.addResources(nodes);
    }

    /**
     * {@inheritDoc}
     * @throws ProActiveException 
     */
    public void addResources(URL descriptorURL) throws ProActiveException {
        aomaster.addResources(descriptorURL);
    }

    /**
     * {@inheritDoc}
     * @throws ProActiveException 
     */
    public void addResources(URL descriptorURL, String virtualNodeName) throws ProActiveException {
        aomaster.addResources(descriptorURL, virtualNodeName);
    }

    /**
     * {@inheritDoc}
     */
    public void addResources(VirtualNode virtualnode) {
        // because of commit 6344, Nodes Creations are not waited anymore before the VirtualNode object is serialized.
        // as a quick dirty fix, we wait manually for Nodes creation if the VirtualNode is activated.
        if (virtualnode.isActivated()) {
            try {
                virtualnode.getNodes();
            } catch (NodeException e) {
                e.printStackTrace();
            }
        }

        aomaster.addResources(virtualnode);
    }

    /**
     * {@inheritDoc}
     */
    public void addResources(String schedulerURL, String user, String password) throws ProActiveException {
        aomaster.addResources(schedulerURL, user, password);
    }

    /**
     * {@inheritDoc}
     */
    public int countAvailableResults() {
        return aomaster.countAvailableResults();
    }

    /**
     * Creates an internal wrapper of the given task
     * This wrapper will identify the task internally via an ID
     * @param task task to be wrapped
     * @return wrapped version
     * @throws TaskAlreadySubmittedException if the same task has already been wrapped
     */
    private long createId(T task) throws TaskAlreadySubmittedException {
        return aorepository.addTask(task, task.hashCode());
    }

    /**
     * Creates an internal version of the given collection of tasks
     * This wrapper will identify the task internally via an ID
     * @param tasks collection of tasks to be wrapped
     * @return wrapped version
     * @throws TaskAlreadySubmittedException if the same task has already been wrapped
     */
    private List<Long> createIds(List<T> tasks) throws TaskAlreadySubmittedException {
        List<Long> wrappings = new ArrayList<Long>();
        for (T task : tasks) {
            wrappings.add(createId(task));
        }

        return wrappings;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return aomaster.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public void setPingPeriod(long periodMillis) {
        aomaster.setPingPeriod(periodMillis);
    }

    /**
     * {@inheritDoc}
     */
    public void setResultReceptionOrder(
            org.objectweb.proactive.extensions.masterworker.interfaces.Master.OrderingMode mode) {
        aomaster.setResultReceptionOrder(mode);
    }

    /**
     * {@inheritDoc}
     */
    public void setInitialTaskFlooding(int number_of_tasks) {
        aomaster.setInitialTaskFlooding(number_of_tasks);
    }

    /**
     * {@inheritDoc}
     */
    public int workerpoolSize() {
        return aomaster.workerpoolSize();
    }

    /**
     * {@inheritDoc}
     */
    public void solve(List<T> tasks) throws TaskAlreadySubmittedException {
        List<Long> wrappers = createIds(tasks);
        System.out.println("ProActiveMaster.solve() : " + aomaster.getClass().getName());
        aomaster.solveIds(wrappers);
    }

    /**
     * {@inheritDoc}
     */
    public void terminate(boolean freeResources) {
        // we use here the synchronous version
        aomaster.terminateIntern(freeResources);
        aorepository.terminate();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<R> waitAllResults() throws TaskException {
        List<ResultIntern<R>> completed = (List<ResultIntern<R>>) PAFuture.getFutureValue(aomaster
                .waitAllResults());
        List<R> results = new ArrayList<R>();
        for (ResultIntern<R> res : completed) {
            if (res.threwException()) {
                throw new TaskException(res.getException());
            }

            R obj = res.getResult();
            if (obj != null) {
                results.add(obj);
            } else {
                results.add(null);
            }

            aorepository.removeId(res.getId());
        }

        return results;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<R> waitKResults(int k) throws IllegalStateException, IllegalArgumentException, TaskException {
        List<ResultIntern<R>> completed = (List<ResultIntern<R>>) PAFuture.getFutureValue(aomaster
                .waitKResults(k));
        List<R> results = new ArrayList<R>();
        for (ResultIntern<R> res : completed) {
            if (res.threwException()) {
                throw new TaskException(res.getException());
            }

            R obj = res.getResult();
            if (obj != null) {
                results.add(obj);
            } else {
                results.add(null);
            }

            aorepository.removeId(res.getId());
        }

        return results;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public R waitOneResult() throws TaskException {
        ResultIntern<R> completed = (ResultIntern<R>) PAFuture.getFutureValue(aomaster.waitOneResult());
        if (completed.threwException()) {
            throw new TaskException(completed.getException());
        }

        //  we remove the mapping between the task and its wrapper
        aorepository.removeId(completed.getId());
        if (completed.threwException()) {
            throw new TaskException(completed.getException());
        }

        R obj = completed.getResult();
        if (obj != null) {
            return obj;
        }

        return null;
    }

}
