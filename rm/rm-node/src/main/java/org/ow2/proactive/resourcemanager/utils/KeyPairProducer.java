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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;


public class KeyPairProducer {

    private static final Logger LOGGER = Logger.getLogger(KeyPairProducer.class);

    private AtomicInteger numberOfWorkers;

    private BlockingQueue<KeyPair> keyPairs = new LinkedBlockingQueue<>();

    private long loopTimeout = 1000; // in milliseconds

    // for test purpose, e.g TaskLauncherTest
    public KeyPairProducer() {

    }

    public KeyPairProducer(int numberOfworkers) {
        // let say we generate 3 times more keys than nodes there are
        this.numberOfWorkers = new AtomicInteger(numberOfworkers * 3);
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        // if we obeserve less than 'numberOfWorkers' elements than we going to
                        // add 'numberOfWorkers' more
                        int numberOfKeysToAdd = numberOfWorkers.get() - keyPairs.size();
                        if (numberOfKeysToAdd > 0) {
                            for (int i = 0; i < numberOfKeysToAdd; ++i) {
                                keyPairs.put(generateAndAddKeyPairs());
                            }
                        }

                        // sleep 1 second
                        synchronized (keyPairs) {
                            keyPairs.wait(loopTimeout);
                        }
                    } catch (InterruptedException e) {
                        LOGGER.error("Deamon thread was interrupted.", e);
                        Thread.currentThread().interrupt();
                    } catch (Throwable e) {
                        // ignore all exception except InterruptedException
                        LOGGER.error(e);
                    }
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
            keyPair = generateAndAddKeyPairs();
        }
        return keyPair;
    }

    public KeyPair generateAndAddKeyPairs() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024, new SecureRandom());
        return keyGen.generateKeyPair();
    }

}
