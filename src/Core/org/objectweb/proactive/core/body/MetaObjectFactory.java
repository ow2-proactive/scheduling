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
package org.objectweb.proactive.core.body;

import java.util.Map;

import org.objectweb.proactive.core.body.ft.protocols.FTManagerFactory;
import org.objectweb.proactive.core.body.migration.MigrationManagerFactory;
import org.objectweb.proactive.core.body.reply.ReplyReceiverFactory;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.body.request.RequestQueueFactory;
import org.objectweb.proactive.core.body.request.RequestReceiverFactory;
import org.objectweb.proactive.core.component.identity.ProActiveComponentFactory;
import org.objectweb.proactive.core.group.spmd.ProActiveSPMDGroupManagerFactory;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.util.ThreadStoreFactory;


/**
 * <p>
 * A class implementing this interface if able to provide instances of factories
 * that can create MetaObjects used in the Body.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2002/05
 * @since   ProActive 0.9.2
 */
public interface MetaObjectFactory {

    /**
     * Creates or reuses a RequestFactory
     * @return a new or existing RequestFactory
     * @see RequestFactory
     */
    public RequestFactory newRequestFactory();

    /**
     * Creates or reuses a ReplyReceiverFactory
     * @return a new or existing ReplyReceiverFactory
     * @see ReplyReceiverFactory
     */
    public ReplyReceiverFactory newReplyReceiverFactory();

    /**
     * Creates or reuses a RequestReceiverFactory
     * @return a new or existing RequestReceiverFactory
     * @see RequestReceiverFactory
     */
    public RequestReceiverFactory newRequestReceiverFactory();

    /**
     * Creates or reuses a RequestQueueFactory
     * @return a new or existing RequestQueueFactory
     * @see RequestQueueFactory
     */
    public RequestQueueFactory newRequestQueueFactory();

    /**
     * Creates or reuses a MigrationManagerFactory
     * @return a new or existing MigrationManagerFactory
     * @see MigrationManagerFactory
     */
    public MigrationManagerFactory newMigrationManagerFactory();

    //    /**
    //     * Creates or reuses a RemoteBodyFactory
    //     * @return a new or existing RemoteBodyFactory
    //     * @see RemoteBodyFactory
    //     */
    //    public RemoteBodyFactory newRemoteBodyFactory();

    /**
     * Creates or reuses a ThreadStoreFactory
     * @return a new or existing ThreadStoreFactory
     * @see ThreadStoreFactory
     */
    public ThreadStoreFactory newThreadStoreFactory();

    // GROUP

    /**
     * Creates or reuses a ProActiveGroupManagerFactory
     * @return a new ProActiveGroupManagerFactory
     */
    public ProActiveSPMDGroupManagerFactory newProActiveSPMDGroupManagerFactory();

    /**
     * creates a ProActiveComponentFactory
     * @return a new ProActiveComponentFactory
     */

    // COMPONENTS
    public ProActiveComponentFactory newComponentFactory();

    /**
     * accessor to the parameters of the factory (object-based configurations)
     * @return the parameters of the factory
     */

    // COMPONENTS
    public Map getParameters();

    //SECURITY

    /**
     * Creates the ProActiveSecurityManager
     * @return a new ProActiveSecurityManager
     * @see ProActiveSecurityManager
     */
    public ProActiveSecurityManager getProActiveSecurityManager();

    public void setProActiveSecurityManager(ProActiveSecurityManager psm);

    public Object clone() throws CloneNotSupportedException;

    // FAULT-TOLERANCE

    /**
     * Creates the fault-tolerance manager.
     * @return the fault-tolerance manager.
     */
    public FTManagerFactory newFTManagerFactory();

    // TIMING

    /**
     * A setter for the reductor.
     * @param timitReductor
     */
    public void setTimItReductor(Object timItReductor);

    /**
     * A getter for the reductor stub.
     * @return The stub on the reductor
     */
    public Object getTimItReductor();
}
