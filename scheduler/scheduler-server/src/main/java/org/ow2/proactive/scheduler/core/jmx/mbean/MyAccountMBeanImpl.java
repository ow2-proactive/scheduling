/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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

    public MyAccountMBeanImpl(final SchedulerAccountsManager accountsManager) throws NotCompliantMBeanException {
        super(MyAccountMBean.class);
        this.accountsManager = accountsManager;
        this.perThreadAccount = new ThreadLocal<>();
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
        this.perThreadAccount = new ThreadLocal<>();
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
    public Object getAttribute(final String attribute)
            throws AttributeNotFoundException, MBeanException, ReflectionException {
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
