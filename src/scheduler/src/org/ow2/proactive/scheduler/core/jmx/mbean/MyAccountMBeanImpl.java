/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core.jmx.mbean;

import java.security.AccessController;

import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.ReflectionException;
import javax.management.StandardMBean;
import javax.security.auth.Subject;

import org.ow2.proactive.scheduler.core.account.SchedulerAccount;
import org.ow2.proactive.scheduler.core.account.SchedulerAccountsManager;


/**
 * Implementation of the MyAccountMBean interface.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public class MyAccountMBeanImpl extends StandardMBean implements MyAccountMBean {

    protected final SchedulerAccountsManager accountsManager;

    private final ThreadLocal<SchedulerAccount> perThreadAccount;

    public MyAccountMBeanImpl(final SchedulerAccountsManager accountsManager)
            throws NotCompliantMBeanException {
        super(MyAccountMBean.class);
        this.accountsManager = accountsManager;
        this.perThreadAccount = new ThreadLocal<SchedulerAccount>();
    }

    /**
     * Creates a new instance of this class. Only for sub-classes.
     *
     * @param mbeanInterface The interface of the MBean
     * @param accountsManager the accounts manager
     * @throws NotCompliantMBeanException if the {@link MyAccountMBean} interface does not follow
     * JMX design patterns for Management Interfaces, or if <var>this</var> does not
     * implement the specified interface.
     */
    protected MyAccountMBeanImpl(final Class<?> mbeanInterface, final SchedulerAccountsManager accountsManager)
            throws NotCompliantMBeanException {
        super(mbeanInterface);
        this.accountsManager = accountsManager;
        this.perThreadAccount = new ThreadLocal<SchedulerAccount>();
    }

    public boolean isConnected() {
        return this.perThreadAccount.get().isConnected();
    }

    public long getConnectionTime() {
        return this.perThreadAccount.get().getConnectionTime();
    }

    public long getLastSubmitTime() {
        return this.perThreadAccount.get().getLastSubmitTime();
    }

    public int getSubmitCount() {
        return this.perThreadAccount.get().getSubmitCount();
    }

    public String getHostname() {
        return this.perThreadAccount.get().getHostname();
    }

    public int getTotalTaskCount() {
        return this.perThreadAccount.get().getTotalTaskCount();
    }

    public long getTotalTaskDuration() {
        return this.perThreadAccount.get().getTotalTaskDuration();
    }

    public int getTotalJobCount() {
        return this.perThreadAccount.get().getTotalJobCount();
    }

    public long getTotalJobDuration() {
        return this.perThreadAccount.get().getTotalJobDuration();
    }

    @Override
    public Object getAttribute(final String attribute) throws AttributeNotFoundException, MBeanException,
            ReflectionException {
        this.setPerThreadAccount();
        return super.getAttribute(attribute);
    }

    @Override
    public AttributeList getAttributes(final String[] attributes) {
        this.setPerThreadAccount();
        return super.getAttributes(attributes);
    }

    private void setPerThreadAccount() {
        // Get the account that corresponds to the username
        final SchedulerAccount acc = this.internalGetAccount();
        this.perThreadAccount.set(acc);
    }

    /**
     * Sub-classes may override this method to return null values.
     * @return the account corresponding to the username
     */
    protected SchedulerAccount internalGetAccount() {
        // Get the current subject from the context
        final Subject subject = Subject.getSubject(AccessController.getContext());
        final String username = subject.getPrincipals().iterator().next().getName();
        SchedulerAccount acc = this.accountsManager.getAccount(username);
        if (acc == null) {
            acc = new SchedulerAccount();
        }
        return acc; // never null
    }
}
