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

import java.security.Permission;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ow2.proactive.permissions.ClientPermission;


public class HandleJobsWithBucketNamePermission extends ClientPermission {
    private final static String DESCRIPTION = "Handle Jobs that contains specific 'bucket_name' in Generic Information";

    public final static String BUCKET_NAME = "bucket_name";

    private Set<String> allowedBuckets = new HashSet<>();

    // permission created by the security file
    public HandleJobsWithBucketNamePermission(String bucketsCommaSeparated) {
        super(DESCRIPTION);
        if (bucketsCommaSeparated != null) {
            allowedBuckets.addAll(Stream.of(bucketsCommaSeparated.split("\\s*,\\s*"))
                                        .map(String::trim)
                                        .collect(Collectors.toSet()));
        }
    }

    // persmission of a job that needs to be authorised
    public HandleJobsWithBucketNamePermission(Map<String, String> genericInformations) {
        super(DESCRIPTION);
        if (genericInformations.containsKey(BUCKET_NAME)) {
            String bucketName = genericInformations.get(BUCKET_NAME);
            this.allowedBuckets = Collections.singleton(bucketName);
        } else {
            this.allowedBuckets = Collections.EMPTY_SET;
        }
    }

    /**
     * check that the given permission matches with this permission
     */
    @Override
    public boolean implies(Permission p) {
        if (!(p instanceof HandleJobsWithBucketNamePermission)) {
            return false;
        }

        HandleJobsWithBucketNamePermission incomingPermission = (HandleJobsWithBucketNamePermission) p;
        // check incoming permission and permission given by the security file
        if (incomingPermission.allowedBuckets.size() == 1) {
            String incomingBucketName = incomingPermission.allowedBuckets.stream().findFirst().get();
            return this.allowedBuckets.contains(incomingBucketName);
        } else {
            return false;
        }
    }

}
