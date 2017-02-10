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
package org.ow2.proactive.scheduler.task.utils;

import java.io.Serializable;
import java.security.KeyException;
import java.security.PrivateKey;

import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;


/**
 * Decrypter is used to ensure one shot usage of private key for a decryption.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public final class Decrypter implements Serializable {

    private PrivateKey key;

    private Credentials credentials;

    /**
     * Create a new instance of Decrypter
     *
     * @param key the private key that will be used for decryption
     */
    public Decrypter(PrivateKey key) {
        if (key == null) {
            throw new IllegalArgumentException("Given key cannot be null");
        }
        this.key = key;
    }

    /**
     * Set the credentials to be decrypted.
     * This method is not mandatory. It allows to store the credentials temporarily.
     * A call to {@link #decrypt()} will clear the key and credentials.
     *
     * @param cred the credentials to be decrypted.
     */
    public void setCredentials(Credentials cred) {
        this.credentials = cred;
    }

    /**
     * Decrypt the stored credential with this object private key.
     *
     * @return the decrypted credData
     * @throws IllegalAccessException if the key is null or have already been used to decrypt a credential
     * 			or if no credentials has been provided before this call.
     * @throws java.security.KeyException decryption failure, malformed data
     */
    public CredData decrypt() throws IllegalAccessException, KeyException {
        if (this.credentials == null || this.key == null) {
            throw new IllegalAccessException("Cannot decrypt credentials !");
        }
        return this.credentials.decrypt(this.key);
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
