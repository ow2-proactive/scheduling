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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.scheduler;

import java.security.KeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MOPException;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.exception.InternalSchedulerException;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.util.CachingSchedulerProxyUserInterface;
import org.ow2.proactive.utils.console.MBeanInfoViewer;

/**
 * Class that extends SchedulerProxyUserInterface as released in 3.0.x
 *
 *
 */
public class MyCachingSchedulerProxyUserInterface extends CachingSchedulerProxyUserInterface {


    /**
     * initialize the connection the scheduler.
     * Must be called only once.
     * Create the corresponding credential object before sending it
     * to the scheduler.
     * @param url the scheduler's url
     * @param user the username to use
     * @param pwd the password to use
     * @param myEventsOnly if true only listen to user events
     * @throws SchedulerException thrown if the scheduler is not available
     * @throws LoginException if the couple username/password is invalid
     */
    public void init(String url, String user, String pwd, boolean myEventsOnly) throws SchedulerException, LoginException {
        SchedulerAuthenticationInterface auth = SchedulerConnection.join(url);
        PublicKey pubKey = auth.getPublicKey();

        try {
            Credentials cred = Credentials.createCredentials(new CredData(CredData.parseLogin(user), CredData
                    .parseDomain(user), pwd), pubKey);
            this.uischeduler = auth.login(cred);
        } catch (KeyException e) {
            throw new InternalSchedulerException(e);
        }  
        
        
        /*
         * very bad hack, change the active object signature  
         */
        Body body = PAActiveObject.getBodyOnThis();
        
        CachingSchedulerProxyUserInterface ao = null;
        
        try {
            ao =  (CachingSchedulerProxyUserInterface) MOP.createStubObject(body.getReifiedObject(), new Object[] { body }, 
            		CachingSchedulerProxyUserInterface.class.getName(), null);
        } catch (MOPException e) {
            throw new ProActiveRuntimeException("Cannot create Stub for this Body e=", e);
        }
        
        schedulerState = this.uischeduler.addEventListener(ao, myEventsOnly, true);
        schedulerState = PAFuture.getFutureValue(schedulerState);
    }

    /**
     * initialize the connection the scheduler.
     * Must be called only once.
     * Create the corresponding credential object before sending it
     * to the scheduler.
     * @param url the scheduler's url
     * @param credData credentials
     * @param myEventsOnly only retrieve user events
     * @throws SchedulerException thrown if the scheduler is not available
     * @throws LoginException if the couple username/password is invalid
     */
    public void init(String url, CredData credData, boolean myEventsOnly) throws SchedulerException, LoginException {
        SchedulerAuthenticationInterface auth = SchedulerConnection.join(url);
        PublicKey pubKey = auth.getPublicKey();

        try {
            Credentials cred = Credentials.createCredentials(credData, pubKey);
            this.uischeduler = auth.login(cred);
            mbeaninfoviewer = new MBeanInfoViewer(auth, credData.getLogin(), cred);
        } catch (KeyException e) {
            throw new InternalSchedulerException(e);
        }
        
        /*
         * very bad hack, change the active object signature  
         */
        Body body = PAActiveObject.getBodyOnThis();
        
        CachingSchedulerProxyUserInterface ao = null;
        
        try {
            ao =  (CachingSchedulerProxyUserInterface) MOP.createStubObject(body.getReifiedObject(), new Object[] { body }, 
            		CachingSchedulerProxyUserInterface.class.getName(), null);
        } catch (MOPException e) {
            throw new ProActiveRuntimeException("Cannot create Stub for this Body e=", e);
        }
        
        
        schedulerState = this.uischeduler.addEventListener(ao, myEventsOnly, true);
        schedulerState = PAFuture.getFutureValue(schedulerState);
    }

    public  HashMap<AtomicLong, LightSchedulerState> getLightSchedulerState() {
    	Map<AtomicLong, SchedulerState> _schedStateAndRevision = getRevisionVersionAndSchedulerState();
    	
    	Entry<AtomicLong, SchedulerState> entry = _schedStateAndRevision.entrySet().iterator().next();
        SchedulerState state = entry.getValue();
        List<JobState> jobs = new ArrayList<JobState>();
        List<UserJobInfo> jobInfoList = new ArrayList<UserJobInfo>();
        
        
        jobs.addAll(state.getPendingJobs());
        jobs.addAll(state.getRunningJobs());
        jobs.addAll(state.getFinishedJobs());
        
        for (JobState j : jobs) {
            jobInfoList.add(new UserJobInfo(j.getId().value(), j.getOwner(), j.getJobInfo()));
        }
        List<UserIdentification> users = new ArrayList<UserIdentification>();
        users.addAll(state.getUsers().getUsers());
        
		LightSchedulerState lightState = new LightSchedulerState(jobInfoList,
				users, state.getStatus());
        
        HashMap<AtomicLong, LightSchedulerState> result = new HashMap<AtomicLong, LightSchedulerState>();
        result.put(entry.getKey(), lightState);
        return  result;
    }
}
