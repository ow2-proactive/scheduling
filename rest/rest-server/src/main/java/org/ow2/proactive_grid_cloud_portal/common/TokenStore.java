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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * TokenStore is used to store the one-time authentication tokens.
 * It stores a map of token and sessionId.
 * A token is only usable once, then it's removed and no longer valid.
 */
public class TokenStore {
    private static TokenStore tokenStoreInstance;

    // The tokens (with their corresponding sessionId) which has been sent to the user, but not yet used.
    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    private TokenStore() {
    }

    public static TokenStore getInstance() {
        if (tokenStoreInstance == null) {
            tokenStoreInstance = new TokenStore();
        }
        return tokenStoreInstance;
    }

    public boolean exists(String token) {
        return tokens.containsKey(token);
    }

    public String getSessionId(String token) {
        return tokens.remove(token);
    }

    public String createToken(String sessionId) {
        String token = TokenGenerator.newToken();
        tokens.put(token, sessionId);
        return token;
    }
}
