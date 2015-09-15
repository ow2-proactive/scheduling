/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;


public class SchedulerTestConfiguration {

    public static final URL RM_DEFAULT_CONFIGURATION = TestScheduler.class
            .getResource("/functionaltests/config/functionalTRMProperties.ini");

    public static final URL SCHEDULER_DEFAULT_CONFIGURATION = TestScheduler.class
            .getResource("/functionaltests/config/functionalTSchedulerProperties.ini");

    public static final SchedulerTestConfiguration NOT_STARTED = new SchedulerTestConfiguration();

    private String schedulerConfigFile;
    private String rmConfigFile;
    private boolean localNodes;
    private int pnpPort;
    private String rmToConnectTo;

    private SchedulerTestConfiguration() {

    }

    public SchedulerTestConfiguration(String schedulerConfigFile, String rmConfigFile, boolean localNodes,
            int pnpPort, String rmToConnectTo) {
        try {
            if (schedulerConfigFile == null) {
                this.schedulerConfigFile = new File(SCHEDULER_DEFAULT_CONFIGURATION.toURI())
                        .getAbsolutePath();

            } else {
                this.schedulerConfigFile = schedulerConfigFile;
            }
            if (rmConfigFile == null) {
                this.rmConfigFile = new File(RM_DEFAULT_CONFIGURATION.toURI()).getAbsolutePath();
            } else {
                this.rmConfigFile = rmConfigFile;
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        this.localNodes = localNodes;
        this.pnpPort = pnpPort;
        this.rmToConnectTo = rmToConnectTo;
    }

    public int getPnpPort() {
        return pnpPort;
    }

    public boolean hasLocalNodes() {
        return localNodes;
    }

    public String getSchedulerConfigFile() {
        return schedulerConfigFile;
    }

    public String getRMConfigFile() {
        return rmConfigFile;
    }

    public String getRMToConnectTo() {
        return rmToConnectTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SchedulerTestConfiguration that = (SchedulerTestConfiguration) o;

        return localNodes == that.localNodes &&
            pnpPort == that.pnpPort &&
            !(schedulerConfigFile != null ? !schedulerConfigFile.equals(that.schedulerConfigFile)
                    : that.schedulerConfigFile != null) &&
            !(rmConfigFile != null ? !rmConfigFile.equals(that.rmConfigFile) : that.rmConfigFile != null) &&
            !(rmToConnectTo != null ? !rmToConnectTo.equals(that.rmToConnectTo) : that.rmToConnectTo != null);

    }

    @Override
    public int hashCode() {
        int result = schedulerConfigFile != null ? schedulerConfigFile.hashCode() : 0;
        result = 31 * result + (rmConfigFile != null ? rmConfigFile.hashCode() : 0);
        result = 31 * result + (localNodes ? 1 : 0);
        result = 31 * result + pnpPort;
        result = 31 * result + (rmToConnectTo != null ? rmToConnectTo.hashCode() : 0);
        return result;
    }

    public static SchedulerTestConfiguration emptyResourceManager() {
        return new SchedulerTestConfiguration(null, null, false, TestScheduler.PNP_PORT, null);
    }

    public static SchedulerTestConfiguration customSchedulerConfig(String configuration) {
        return new SchedulerTestConfiguration(configuration, null, true, TestScheduler.PNP_PORT, null);
    }

    public static SchedulerTestConfiguration defaultConfiguration() {
        return new SchedulerTestConfiguration(null, null, true, TestScheduler.PNP_PORT, null);
    }

    @Override
    public String toString() {
        return "SchedulerTestConfiguration{" +
          "schedulerConfigFile='" + new File(schedulerConfigFile).getName() + '\'' +
          ", rmConfigFile='" + new File(rmConfigFile).getName() + '\'' +
          ", localNodes=" + localNodes +
          ", pnpPort=" + pnpPort +
          ", rmToConnectTo='" + rmToConnectTo + '\'' +
          '}';
    }
}
