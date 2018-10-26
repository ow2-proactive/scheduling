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
package org.ow2.proactive_grid_cloud_portal.common;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.AbstractMap;

import org.apache.commons.configuration2.Configuration;
import org.jose4j.jwt.JwtClaims;
import org.mockito.Matchers;
import org.ow2.proactive.boot.microservices.iam.IAMStarter;
import org.ow2.proactive.boot.microservices.iam.util.IAMConfiguration;
import org.ow2.proactive.boot.microservices.iam.util.IAMSessionUtil;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


public class IAMTestUtil {

    private static final String IAM_LOGIN_METHOD = "IAMLoginMethod";

    public static final Boolean IAM_IS_USED = PASchedulerProperties.SCHEDULER_LOGIN_METHOD.getValueAsString()
                                                                                          .equals(IAM_LOGIN_METHOD) &&
                                              PAResourceManagerProperties.RM_LOGIN_METHOD.getValueAsString()
                                                                                         .equals(IAM_LOGIN_METHOD);

    public static String ssoTicket = "TGT-1-xxxxxxxx";

    public static String jwtToken = "jwtTokenxxxxxxx";

    public static String login = "user";

    public static String password = "pwd";

    public static void setUpIAM() {

        Configuration config = IAMStarter.getConfiguration();

        config.setProperty(IAMConfiguration.IAM_TOKEN_SIGNATURE_ENABLED, true);
        config.setProperty(IAMConfiguration.IAM_TOKEN_ENCRYPTION_ENABLED, true);
        config.setProperty(IAMConfiguration.IAM_TOKEN_SIGNATURE_KEY, "");
        config.setProperty(IAMConfiguration.IAM_TOKEN_ENCRYPTION_KEY, "");
        config.setProperty(IAMConfiguration.PA_REST_SESSION_AS_JWT, true);
        config.setProperty(IAMConfiguration.SSO_TICKET_MARKER, "");

        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setJwtId(ssoTicket);

        AbstractMap.SimpleEntry<String, JwtClaims> iamToken = new AbstractMap.SimpleEntry<String, JwtClaims>(jwtToken,
                                                                                                             new JwtClaims());
        IAMSessionUtil iamSessionUtil = mock(IAMSessionUtil.class);
        when(iamSessionUtil.createNewSessionToken(Matchers.anyString(), Matchers.any())).thenReturn(iamToken);
        when(iamSessionUtil.tokenIsValid(Matchers.anyString())).thenReturn(true);
        when(iamSessionUtil.deleteToken(Matchers.anyString())).thenReturn(true);
        when(iamSessionUtil.isJWTSession()).thenReturn(true);

        SharedSessionStore.getInstance().setIamSessionUtil(iamSessionUtil);
    }
}
