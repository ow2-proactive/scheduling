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
package org.ow2.proactive.scheduler.core.db;

import javax.persistence.*;

import java.io.Serializable;


@Entity
@NamedQueries( {
        @NamedQuery(
                name = "deleteThirdPartyCredentialsKeySetByUsernameAndKey",
                query = "delete from ThirdPartyCredentialData where username = :username and key = :key"
        ),
        @NamedQuery(
                name = "findThirdPartyCredentialsKeySetByUsername",
                query = "select key from ThirdPartyCredentialData where username = :username"
        ),
        @NamedQuery(
                name = "findThirdPartyCredentialsMapByUsername",
                query = "select key, encryptedSymmetricKey, encryptedValue " +
                        "from ThirdPartyCredentialData " + "where username = :username"
        ),
        @NamedQuery(
                name = "hasThirdPartyCredentials",
                query = "select count(*) from ThirdPartyCredentialData where username = :username"
        ),
        @NamedQuery(name = "countThirdPartyCredentialData", query = "select count (*) from ThirdPartyCredentialData")
})
@Table(name = "THIRD_PARTY_CREDENTIAL_DATA")
public class ThirdPartyCredentialData implements Serializable {

    // fix for #2456 : Credential Data and TaskLogs contain serialVersionUID based on scheduler server version
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "USERNAME")
    String username;

    @Id
    @Column(name = "CREDENTIAL_KEY")
    String key;

    @Column(name = "ENCRYPTED_SYMMETRIC_KEY", length = Integer.MAX_VALUE)
    @Lob
    byte[] encryptedSymmetricKey;

    @Column(name = "ENCRYPTED_CREDENTIAL_VALUE", length = Integer.MAX_VALUE)
    @Lob
    byte[] encryptedValue;

    public ThirdPartyCredentialData() {
    }

    public ThirdPartyCredentialData(String username, String key, byte[] encryptedSymmetricKey, byte[] encryptedValue) {
        this.username = username;
        this.key = key;
        this.encryptedSymmetricKey = encryptedSymmetricKey;
        this.encryptedValue = encryptedValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ThirdPartyCredentialData that = (ThirdPartyCredentialData) o;

        return !(key != null ? !key.equals(that.key) : that.key != null) &&
               !(username != null ? !username.equals(that.username) : that.username != null);

    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }

}
