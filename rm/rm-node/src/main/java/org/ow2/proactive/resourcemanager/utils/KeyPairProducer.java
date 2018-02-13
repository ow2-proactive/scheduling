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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;


public class KeyPairProducer {

    private static final Logger LOGGER = Logger.getLogger(KeyPairProducer.class);

    // should be aligned with time unit used in Object::wait()
    private static final TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    private AtomicInteger numberOfWorkers;

    private Future<?> future;

    private BlockingQueue<KeyPair> keyPairs = new LinkedBlockingQueue<>();

    private long loopTimeout = 1000;

    public KeyPairProducer() {
        future = null;
    }

    public KeyPairProducer(int numberOfworkers) {
        this.numberOfWorkers = new AtomicInteger(numberOfworkers);
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        future = executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        // if we obeserve less than 'numberOfWorkers' elements than we going to
                        // add 'numberOfWorkers' more
                        int numberOfKeysToAdd = numberOfWorkers.get() - keyPairs.size();
                        if (numberOfKeysToAdd > 0) {
                            for (int i = 0; i < numberOfKeysToAdd; ++i) {
                                keyPairs.put(generateAndAddKeyPairs());
                            }
                        }

                        // sleep 1 second, ot until notification (from getKeyPair method)
                        synchronized (keyPairs) {
                            keyPairs.wait(loopTimeout);
                        }
                    } catch (InterruptedException e) {
                        LOGGER.error("Thread was interrupted.", e);
                        Thread.currentThread().interrupt();
                    } catch (Throwable e) {
                        // ignore all exception except InterruptedException
                        LOGGER.error(e);
                    }
                }
            }
        });
    }

    /**
     * @return keyPair from head of the keyPairs queue
     */
    public KeyPair getKeyPair() throws NoSuchAlgorithmException {
        // if by change queue is empty then we notify background thread which apparently sleeps
        // so that we do not wait whole timeout.
        if (keyPairs.isEmpty()) {
            synchronized (keyPairs) {
                keyPairs.notify();
            }
        }
        try {
            // block until there is something in the queue but do not block more than 3 timeouts
            KeyPair keyPair = null;
            if (future != null && !future.isDone()) {
                keyPair = keyPairs.poll(loopTimeout * 3, timeUnit);
                if (keyPair == null) {
                    LOGGER.warn("Background thread is still working but there is nothing in the queue, so we generate key synchronously.");
                    keyPair = generateAndAddKeyPairs();
                }
            } else {
                //LOGGER.error("Background thread is dead, so we generate key synchronously.");
                keyPair = generateAndAddKeyPairs();
            }
            return keyPair;
        } catch (InterruptedException e) {
            LOGGER.error(e);
            return generateAndAddKeyPairs();
        }
    }

    public int getNumberOfWorkers() {
        return numberOfWorkers.get();
    }

    public KeyPair generateAndAddKeyPairs() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024, new SecureRandom());
        return keyGen.generateKeyPair();
    }

}
