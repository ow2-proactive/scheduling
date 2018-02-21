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
package org.ow2.proactive.resourcemanager.utils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;


public class KeyPairProducer {

    private static final Logger LOGGER = Logger.getLogger(KeyPairProducer.class);

    private BlockingQueue<KeyPair> keyPairs;

    // for test purpose, e.g TaskLauncherTest
    public KeyPairProducer() {
        keyPairs = new LinkedBlockingQueue<>(1);
    }

    public KeyPairProducer(int numberOfworkers) {
        this.keyPairs = new LinkedBlockingQueue(numberOfworkers);
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        keyPairs.put(generateAndAddKeyPairs());
                    }
                } catch (InterruptedException e) {
                    LOGGER.error(e);
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * @return keyPair from head of the keyPairs queue or generates synchronously
     */
    public KeyPair getKeyPair() throws NoSuchAlgorithmException {
        KeyPair keyPair = keyPairs.poll();
        if (keyPair == null) {
            // if by change queue is empty then we generate key pair synchronously
            LOGGER.warn("Key pair queue is empty, generating a key pair now");
            keyPair = generateAndAddKeyPairs();
        }
        return keyPair;
    }

    private KeyPair generateAndAddKeyPairs() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024, new SecureRandom());
        return keyGen.generateKeyPair();
    }

}
