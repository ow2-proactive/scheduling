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
package functionaltests;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public class RestFuncTestConfig {

    public static final String RESTAPI_TEST_LOGIN = "restapi.test.login";

    public static final String RESTAPI_TEST_NON_ADMIN_LOGIN = "restapi.test.non-admin.login";

    public static final String RESTAPI_TEST_NON_ADMIN_LOGIN_PASSWORD = "restapi.test.non-admin.login.password";

    public static final String RESTAPI_TEST_PASSWORD = "restapi.test.password";

    public static final String RESTAPI_TEST_SCHEDULER_HOME = PASchedulerProperties.SCHEDULER_HOME.getKey();

    public static final String RESTAPI_TEST_RM_HOME = PAResourceManagerProperties.RM_HOME.getKey();

    public static final String RESTAPI_TEST_PORT = "restapi.test.port";

    private static final RestFuncTestConfig instance = new RestFuncTestConfig();

    private Properties props;

    private RestFuncTestConfig() {
        try {
            init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized static RestFuncTestConfig getInstance() throws Exception {
        return instance;
    }

    private void init() throws Exception {
        props = new Properties();
        URL url = RestFuncTestConfig.class.getResource("config/restapi-test.properties");
        FileInputStream fis = new FileInputStream(new File(url.toURI()));
        props.load(fis);
        props.putAll(System.getProperties());
    }

    public String getLogin() {
        return props.getProperty(RESTAPI_TEST_LOGIN);
    }

    public String getNonAdminLogin() {
        return props.getProperty(RESTAPI_TEST_NON_ADMIN_LOGIN);
    }

    public String getPassword() {
        return props.getProperty(RESTAPI_TEST_PASSWORD);
    }

    public String getNonAdminLonginPassword() {
        return props.getProperty(RESTAPI_TEST_NON_ADMIN_LOGIN_PASSWORD);
    }

    public String setProperty(String key, String value) {
        return (String) props.setProperty(key, value);
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

}
