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
package org.ow2.proactive.scheduler.permissions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;


public class HandleJobsWithBucketNamePermissionTest {

    @Test
    public void testThatJobAllowed() {
        HandleJobsWithBucketNamePermission fromSecutiryFile = new HandleJobsWithBucketNamePermission("uno,dos,trez");

        Map<String, String> genericInfo = new HashMap<>();
        genericInfo.put(HandleJobsWithBucketNamePermission.BUCKET_NAME, "uno");

        HandleJobsWithBucketNamePermission persmissionFromJob = new HandleJobsWithBucketNamePermission(genericInfo);

        assertTrue(fromSecutiryFile.implies(persmissionFromJob));
    }

    @Test
    public void testThatJobAllowed2dBucket() {
        HandleJobsWithBucketNamePermission fromSecutiryFile = new HandleJobsWithBucketNamePermission("uno,dos,trez");

        Map<String, String> genericInfo = new HashMap<>();
        genericInfo.put(HandleJobsWithBucketNamePermission.BUCKET_NAME, "doz");

        HandleJobsWithBucketNamePermission persmissionFromJob = new HandleJobsWithBucketNamePermission(genericInfo);

        assertTrue(fromSecutiryFile.implies(persmissionFromJob));
    }

    @Test
    public void testThatJobAllowed3rdBucket() {
        HandleJobsWithBucketNamePermission fromSecutiryFile = new HandleJobsWithBucketNamePermission("uno,dos,trez");

        Map<String, String> genericInfo = new HashMap<>();
        genericInfo.put(HandleJobsWithBucketNamePermission.BUCKET_NAME, "trez");

        HandleJobsWithBucketNamePermission persmissionFromJob = new HandleJobsWithBucketNamePermission(genericInfo);

        assertTrue(fromSecutiryFile.implies(persmissionFromJob));
    }

    @Test
    public void testThatJobForbiden() {
        HandleJobsWithBucketNamePermission fromSecutiryFile = new HandleJobsWithBucketNamePermission("uno,dos,trez");

        Map<String, String> genericInfo = new HashMap<>();
        genericInfo.put(HandleJobsWithBucketNamePermission.BUCKET_NAME, "quatro");

        HandleJobsWithBucketNamePermission persmissionFromJob = new HandleJobsWithBucketNamePermission(genericInfo);

        assertFalse(fromSecutiryFile.implies(persmissionFromJob));
    }

    @Test
    public void testThatJobForbiden2Buckets() {
        HandleJobsWithBucketNamePermission fromSecutiryFile = new HandleJobsWithBucketNamePermission("uno,dos,trez");

        Map<String, String> genericInfo = new HashMap<>();
        genericInfo.put(HandleJobsWithBucketNamePermission.BUCKET_NAME, "uno,dos");

        HandleJobsWithBucketNamePermission persmissionFromJob = new HandleJobsWithBucketNamePermission(genericInfo);

        assertFalse(fromSecutiryFile.implies(persmissionFromJob));
    }

    @Test
    public void testOtherPermissionClassDoesNotBrakeIn() {
        HandleJobsWithBucketNamePermission fromSecutiryFile = new HandleJobsWithBucketNamePermission("uno,dos,trez");

        Map<String, String> genericInfo = new HashMap<>();
        genericInfo.put(HandleJobsWithBucketNamePermission.BUCKET_NAME, "quatro");

        HandleJobsWithGenericInformationPermission persmissionFromJob = new HandleJobsWithGenericInformationPermission(genericInfo);

        assertFalse(fromSecutiryFile.implies(persmissionFromJob));
    }
}
