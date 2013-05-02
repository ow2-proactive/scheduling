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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $PROACTIVE_INITIAL_DEV$
 */
package functionaltests;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;


public class RestFuncTestConfig {

    public static final String RESTAPI_TEST_LOGIN = "restapi.test.login";
    public static final String RESTAPI_TEST_PASSWORD = "restapi.test.password";
    public static final String RESTAPI_TEST_SCHEDULER_HOME = "restapi.test.scheduler.home";
    public static final String RESTAPI_TEST_RM_HOME = "restapi.test.rm.home";
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

    public String getPassword() {
        return props.getProperty(RESTAPI_TEST_PASSWORD);
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

}
