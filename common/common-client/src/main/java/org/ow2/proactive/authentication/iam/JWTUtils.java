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
package org.ow2.proactive.authentication.iam;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;


public class JWTUtils {

    private JWTUtils() {

    }

    /**
     * Tries to parse specified String as a JWT token. If successful, returns the set of claims (extracted from the token body).
     * If unsuccessful (token is invalid or not containing all required properties), simply returns null.
     *
     * @param token the JWT token to parse
     * @return the User object extracted from specified token or null if a token is invalid.
     */
    public static Claims parseToken(String token) {

        Claims claims = new DefaultClaims();

        JsonFactory factory = new JsonFactory();
        try (JsonParser parser = factory.createParser(token)) {

            while (!parser.isClosed()) {

                JsonToken jsonToken = parser.nextToken();

                if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                    String key = parser.getCurrentName();

                    parser.nextToken();
                    String value = parser.getValueAsString();

                    claims.put(key, value);
                }

            }

        } catch (IOException ioe) {
            throw new IAMException("Failed to parse JWT acquired from IAM: " + token, ioe);
        }

        return claims;
    }
}
