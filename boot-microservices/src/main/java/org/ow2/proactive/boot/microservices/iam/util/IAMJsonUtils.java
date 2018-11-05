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
package org.ow2.proactive.boot.microservices.iam.util;

import java.util.Map;

import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;
import org.jose4j.json.internal.json_simple.parser.ParseException;


public class IAMJsonUtils {

    /**
     * Attributes used to parse a (json) credentials validation answer from IAM
     * Example of json answer:
     * {"credentialProvided":true,"authentication":{"@class":"org.apereo.cas.authentication.DefaultAuthentication",
     * "authenticationDate":1540372322.604000000,"principal":{"@class":"org.apereo.cas.authentication.principal.SimplePrincipal",
     * "id":"scheduler","attributes":{"cn":"scheduler","role":"user"}},
     * "credentials":[{"@class":"org.apereo.cas.authentication.BasicCredentialMetaData",
     * "id":"scheduler","credentialClass":"org.apereo.cas.authentication.UsernamePasswordCredential"}],
     * "attributes":{"credentialType":"UsernamePasswordCredential",
     * "credential":"eyJhbGciOiJIUzUxMiJ9.ZXlKNmFYQWlPaUpFUlVZaUxDSmhiR2NpT2lKa2FYSWlMQ0psYm1NaU9pSkJNVEk0UTBKRExVaFRNalUySW4wLi5tdEN0MzEtSjNudHFGbE8ycUttakhnLl9scXBpVTFTczM2VGtRMUg5djA3blEuMTdRRy16bVFWbjJsRlh4N3RXT0RGQQ==.RAjbY75oLLedHnpOz7JamegClT0MkPk4gCoCSclO5B8LYQn_88rF4apCvZ-isL4lIrIIxZl7eIYLYvHwctsZag",
     * "authenticationMethod":"JsonResourceAuthenticationHandler",
     * "successfulAuthenticationHandlers":["JsonResourceAuthenticationHandler"]},
     * "successes":{"JsonResourceAuthenticationHandler":{
     * "@class":"org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult",
     * "handlerName":"JsonResourceAuthenticationHandler","credentialMetaData":{
     * "@class":"org.apereo.cas.authentication.BasicCredentialMetaData","id":"scheduler",
     * "credentialClass":"org.apereo.cas.authentication.UsernamePasswordCredential"},
     * "principal":{"@class":"org.apereo.cas.authentication.principal.SimplePrincipal","id":"scheduler",
     * "attributes":{"cn":"scheduler","role":"user"}},"warnings":[]}},"failures":{}},"service":null}
     */
    private static final String AUTHENTICATION_KEY = "authentication";

    private static final String PRINCIPAL_KEY = "principal";

    private static final String PRINCIPAL_ID_KEY = "id";

    private static final String PRINCIPAL_ATTRIBUTES_KEY = "attributes";

    private Map principal;

    /**
     * parses and validates a JSON string containing authentication and user information acquired
     * from IAM microservice.
     *
     * @param json JSON string containing authentication and user information
     * @throws ParseException if a problem occurs during the parsing process
     * @since version 8.3.0
     */
    public void parseAuthenticationJson(String json) throws ParseException {
        JSONObject authenticationJson = (JSONObject) new JSONParser().parse(json);
        Map authentication = (Map<String, String>) authenticationJson.get(AUTHENTICATION_KEY);
        principal = (Map<String, String>) authentication.get(PRINCIPAL_KEY);
    }

    /**
     * returns the user id parsed from JSON string
     *
     * @return user ID
     * @since version 8.3.0
     */
    public String getUserId() {
        return (String) principal.get(PRINCIPAL_ID_KEY);
    }

    /**
     * returns the user attributes parsed from JSON string
     *
     * @return user attributes as a Map
     * @since version 8.3.0
     */
    public Map<String, String> getUserAttributes() {
        return (Map<String, String>) principal.get(PRINCIPAL_ATTRIBUTES_KEY);
    }
}
