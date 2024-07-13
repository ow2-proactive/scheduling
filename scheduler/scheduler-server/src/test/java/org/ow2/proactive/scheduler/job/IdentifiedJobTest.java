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
package org.ow2.proactive.scheduler.job;

import static org.junit.Assert.*;

import java.util.Collections;

import javax.security.auth.Subject;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.authentication.principals.DomainNamePrincipal;
import org.ow2.proactive.authentication.principals.GroupNamePrincipal;
import org.ow2.proactive.authentication.principals.TenantPrincipal;
import org.ow2.proactive.authentication.principals.UserNamePrincipal;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.tests.ProActiveTestClean;


public class IdentifiedJobTest extends ProActiveTestClean {

    @Test
    public void testJobWithTenantOtherUserWithSameTenantIsNotOwner() {
        UserIdentificationImpl jobIdentification = new UserIdentificationImpl("user", "tenant", "tenant");
        JobId jid = JobIdImpl.makeJobId("1");
        IdentifiedJob job = new IdentifiedJob(jid, jobIdentification, Collections.EMPTY_MAP);
        Subject connectedUserSubject = new Subject();
        connectedUserSubject.getPrincipals().add(new UserNamePrincipal("user2"));
        connectedUserSubject.getPrincipals().add(new GroupNamePrincipal("user"));
        connectedUserSubject.getPrincipals().add(new TenantPrincipal("tenant"));
        connectedUserSubject.getPrincipals().add(new DomainNamePrincipal("tenant"));
        UserIdentificationImpl userIdentification2 = new UserIdentificationImpl("user2", connectedUserSubject);
        Assert.assertFalse(job.hasRight(userIdentification2));
    }

    @Test
    public void testJobWithoutTenantOtherUserWithAnyTenantIsNotOwner() {
        UserIdentificationImpl jobIdentification = new UserIdentificationImpl("user", null, null);
        JobId jid = JobIdImpl.makeJobId("1");
        IdentifiedJob job = new IdentifiedJob(jid, jobIdentification, Collections.EMPTY_MAP);
        Subject connectedUserSubject = new Subject();
        connectedUserSubject.getPrincipals().add(new UserNamePrincipal("user2"));
        connectedUserSubject.getPrincipals().add(new GroupNamePrincipal("user"));
        connectedUserSubject.getPrincipals().add(new TenantPrincipal("tenant"));
        connectedUserSubject.getPrincipals().add(new DomainNamePrincipal("tenant"));
        UserIdentificationImpl userIdentification2 = new UserIdentificationImpl("user2", connectedUserSubject);
        Assert.assertFalse(job.hasRight(userIdentification2));
    }

    @Test
    public void testJobWithDifferentUserIsNotOwner() {
        UserIdentificationImpl jobIdentification = new UserIdentificationImpl("user", null, null);
        JobId jid = JobIdImpl.makeJobId("1");
        IdentifiedJob job = new IdentifiedJob(jid, jobIdentification, Collections.EMPTY_MAP);

        Subject connectedUserSubject = new Subject();
        connectedUserSubject.getPrincipals().add(new UserNamePrincipal("user2"));
        connectedUserSubject.getPrincipals().add(new GroupNamePrincipal("user"));
        UserIdentificationImpl userIdentification2 = new UserIdentificationImpl("user2", connectedUserSubject);

        Assert.assertFalse(job.hasRight(userIdentification2));
    }

    @Test
    public void testJobWithSameUserIsOwner() {
        UserIdentificationImpl jobIdentification = new UserIdentificationImpl("user", null, null);
        JobId jid = JobIdImpl.makeJobId("1");
        IdentifiedJob job = new IdentifiedJob(jid, jobIdentification, Collections.EMPTY_MAP);
        Subject connectedUserSubject = new Subject();
        connectedUserSubject.getPrincipals().add(new UserNamePrincipal("user"));
        connectedUserSubject.getPrincipals().add(new GroupNamePrincipal("user"));
        UserIdentificationImpl userIdentification2 = new UserIdentificationImpl("user", connectedUserSubject);
        Assert.assertTrue(job.hasRight(userIdentification2));
    }

    @Test
    public void testJobWithSameUserAndDifferentTenantIsNotOwner() {
        UserIdentificationImpl jobIdentification = new UserIdentificationImpl("user", "tenant", "tenant");
        JobId jid = JobIdImpl.makeJobId("1");
        IdentifiedJob job = new IdentifiedJob(jid, jobIdentification, Collections.EMPTY_MAP);
        Subject connectedUserSubject = new Subject();
        connectedUserSubject.getPrincipals().add(new UserNamePrincipal("user"));
        connectedUserSubject.getPrincipals().add(new GroupNamePrincipal("user"));
        connectedUserSubject.getPrincipals().add(new TenantPrincipal("tenant2"));
        connectedUserSubject.getPrincipals().add(new DomainNamePrincipal("tenant2"));
        UserIdentificationImpl userIdentification2 = new UserIdentificationImpl("user", connectedUserSubject);
        Assert.assertFalse(job.hasRight(userIdentification2));
    }

}
