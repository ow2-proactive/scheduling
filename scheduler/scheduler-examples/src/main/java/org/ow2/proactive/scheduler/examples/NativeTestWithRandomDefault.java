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
package org.ow2.proactive.scheduler.examples;

import java.util.Random;


/**
 * NativeTestWithRandomDefault is a class that should test native process generating random error code.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class NativeTestWithRandomDefault {

    /**
     * Task that will wait randomly from 1 to 30 milliseconds
     * And will then return an error code 3 times on 5.
     *
     * @param args
     */
    public static void main(String[] args) {
        Random exit = new Random(System.currentTimeMillis());
        int exitStatus = exit.nextInt(5);

        if (exitStatus >= 3) {
            System.out.println("Exit code is : " + (exitStatus - 1));
            System.exit(exitStatus - 1);
        } else {
            System.out.println("Exit code is : 0");
            System.exit(0);
        }
    }

}
