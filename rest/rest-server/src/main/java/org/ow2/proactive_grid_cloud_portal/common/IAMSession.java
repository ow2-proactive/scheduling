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

import org.jose4j.jwt.JwtClaims;


/**
 * IAMSession extends the legacy Session by using a cyphered JWT (Json Web Token) as sessionId (i.e., sessionId is no longer
 * a generated String). Additionally, IAMSession stores the set of plain attributes (i.e., claims) included in the JWT,
 * thus avoiding to decypher the JWT each time some of these attributes are needed.
 */
public class IAMSession extends Session {

    // Set of claims (i.e., attributes) included in JWT
    private JwtClaims jwtClaims;

    /**
     * Public constructor of IAMSession.
     *
     * @param sessionToken session id provided as a JWT (Json Web Token) or a SSO ticket
     * @param jwtClaims Set of claims (i.e., attributes) included in JWT (set
     * @param schedulerRMProxyFactory inherited from (super) Session
     * @param clock inherited from (super) Session
     */
    public IAMSession(String sessionToken, JwtClaims jwtClaims, SchedulerRMProxyFactory schedulerRMProxyFactory,
            Clock clock) {

        // Create a legacy session with a JWT as a sessionId
        super(sessionToken, schedulerRMProxyFactory, clock);
        this.jwtClaims = jwtClaims;

    }

    /**
     * returns the set of claims (i.e., attributes) included in JWT, or null if the session id is a SSO ticket.
     *
     * @return Set of claims (i.e., attributes) included in JWT
     */
    public JwtClaims getJwtClaims() {
        return this.jwtClaims;
    }

}
