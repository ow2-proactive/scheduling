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
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.tests.performance.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.utils.FileToBytesConverter;


public class SchedulingFolder {

    private final File rootDir;

    private final File performanceDir;

    private final File performanceClassesDir;

    private final File testConfigDir;

    private final File testTmpDir;

    public SchedulingFolder(String path) {
        rootDir = new File(path);
        performanceDir = new File(rootDir, "performance");
        performanceClassesDir = new File(rootDir, "classes/performance");
        testConfigDir = new File(performanceDir, "config");
        testTmpDir = new File(performanceDir, "tmp");
    }

    public String getRootDirPath() {
        return rootDir.getAbsolutePath();
    }

    public File getRootDir() {
        return rootDir;
    }

    public File getPerformanceDir() {
        return performanceDir;
    }

    public File getPerformanceClassesDir() {
        return performanceClassesDir;
    }

    public File getTestConfigDir() {
        return testConfigDir;
    }

    public File getTestTmpDir() {
        return testTmpDir;
    }

    public Credentials getRMCredentials() {
        return getCredentials("/config/authentication/rm.cred");
    }

    public byte[] getRMCredentialsBytes() throws IOException {
        return getCredentialsBytes("/config/authentication/rm.cred");
    }

    public Credentials getSchedulingCredentials() {
        return getCredentials("/config/authentication/scheduler.cred");
    }

    public byte[] getSchedulingCredentialsBytes() throws IOException {
        return getCredentialsBytes("/config/authentication/scheduler.cred");
    }

    private byte[] getCredentialsBytes(String path) throws IOException {
        File credentialFile = new File(rootDir, path);
        if (!credentialFile.exists()) {
            throw new TestExecutionException("Can't find credentials file: " +
                credentialFile.getAbsolutePath());
        }
        return FileToBytesConverter.convertFileToByteArray(credentialFile);
    }

    private Credentials getCredentials(String path) {
        File credentialFile = new File(rootDir, path);
        if (!credentialFile.exists()) {
            throw new TestExecutionException("Can't find credentials file: " +
                credentialFile.getAbsolutePath());
        }
        try {
            return Credentials.getCredentials(new FileInputStream(credentialFile));
        } catch (Exception e) {
            throw new TestExecutionException("Failed to create default credentials", e);
        }
    }

}
