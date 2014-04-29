/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
package org.ow2.proactive.scheduler.common.task;

import java.security.KeyException;
import java.security.PrivateKey;

import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;


/**
 * OneShotDecrypter is used to ensure one shot usage of private key for a decryption.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public final class OneShotDecrypter {
    private PrivateKey key = null;
    private Credentials credentials = null;

    /**
     * Create a new instance of OneShotDecrypter
     *
     * @param key the private key that will be used for decryption
     */
    public OneShotDecrypter(PrivateKey key) {
        if (key == null) {
            throw new IllegalArgumentException("Given key cannot be null");
        }
        this.key = key;
    }

    /**
     * Set the credentials to be decrypted.
     * This method is not mandatory. It allows to store the credentials temporarily.
     * A call to {@link #decrypt(Credentials)} or {@link #decrypt()} will clear the key and credentials.
     *
     * @param cred the credentials to be decrypted.
     */
    public void setCredentials(Credentials cred) {
        this.credentials = cred;
    }

    /**
     * Decrypt the given credential with this object private key.
     *
     * @param cred the credentials to be decrypted
     * @return the decrypted credData
     * @throws IllegalAccessException if the key is null or have already been used to decrypt a credential
     * @throws java.security.KeyException decryption failure, malformed data
     */
    public CredData decrypt(final Credentials cred) throws IllegalAccessException, KeyException {
        if (this.key == null) {
            throw new IllegalAccessException("Cannot decrypt credentials !");
        }
        //decrypt
        CredData data = cred.decrypt(this.key);
        //reset key
        this.key = null;
        return data;
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
        if (this.credentials == null) {
            throw new IllegalAccessException("Cannot decrypt credentials !");
        }
        return decrypt(this.credentials);
    }
}
