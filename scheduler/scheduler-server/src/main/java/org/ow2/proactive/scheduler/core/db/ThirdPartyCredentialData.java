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
package org.ow2.proactive.scheduler.core.db;

import javax.persistence.*;
import java.io.Serializable;


@Entity
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

    public ThirdPartyCredentialData(String username, String key, byte[] encryptedSymmetricKey,
            byte[] encryptedValue) {
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
