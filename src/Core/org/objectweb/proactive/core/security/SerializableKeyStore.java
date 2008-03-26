/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;


/**
 * @author The ProActive Team
 *
 */
public class SerializableKeyStore implements Serializable {

    /**
     *
     */
    protected transient KeyStore keyStore;
    protected byte[] encodedKeyStore;

    public SerializableKeyStore() {
        // TODO Auto-generated constructor stub
    }

    public SerializableKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        try {
            this.keyStore.store(bout, "ha".toCharArray());
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.encodedKeyStore = bout.toByteArray();
        bout.close();

        out.defaultWriteObject();
        this.encodedKeyStore = null;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        try {
            this.keyStore = KeyStore.getInstance("PKCS12", "BC");
            this.keyStore.load(new ByteArrayInputStream(this.encodedKeyStore), "ha".toCharArray());
        } catch (KeyStoreException e) {
            // TODOSECURITYSECURITY Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            // TODOSECURITYSECURITY Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODOSECURITYSECURITY Auto-generated catch block
            e.printStackTrace();
        } catch (CertificateException e) {
            // TODOSECURITYSECURITY Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODOSECURITYSECURITY Auto-generated catch block
            e.printStackTrace();
        }
    }

    public KeyStore getKeyStore() {
        return this.keyStore;
    }
}
