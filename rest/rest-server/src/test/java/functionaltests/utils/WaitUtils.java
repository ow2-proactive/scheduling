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
package functionaltests.utils;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;


/**
 * @author ActiveEon Team
 * @since 09/03/20
 */
public class WaitUtils {
    // timeout of each wait function (in seconds), not positive value means no timeout, default is no timeout.
    private int timeoutSeconds = -1;

    // interval between recheck the condition (in seconds), default is 1 second.
    private int retryIntervalSeconds = 1;

    public WaitUtils() {
    }

    public WaitUtils(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public WaitUtils(int timeoutSeconds, int retryIntervalSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        this.retryIntervalSeconds = retryIntervalSeconds;
    }

    /**
     * Wait until the condition is matched.
     * The condition is rechecked in each specify interval seconds.
     * If the condition is still not matched until reaching the specified timeout, an exception will be thrown.
     *
     * @param conditionToMatch the waited condition
     * @param conditionDescription the description of the waited condition (used in the log and the timeout error message)
     */
    public void until(BooleanSupplier conditionToMatch, String conditionDescription) {
        System.out.printf("%s Start waiting for %s%n", LocalTime.now(), conditionDescription);
        int waitedTime = 0;
        while (!conditionToMatch.getAsBoolean()) {
            sleep(retryIntervalSeconds);
            waitedTime += retryIntervalSeconds;
            if (timeoutSeconds > 0 && waitedTime >= timeoutSeconds) {
                throw new IllegalStateException(String.format("Timeout Error: the condition [%s] is still not matched after the expected timeout: [%d] seconds",
                                                              conditionDescription,
                                                              timeoutSeconds));
            }
        }
        System.out.printf("%s Matched the condition: %s%n", LocalTime.now(), conditionDescription);
    }

    public static void sleep(int waitSeconds) {
        try {
            TimeUnit.SECONDS.sleep(waitSeconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
