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
package org.ow2.proactive.scheduler.common;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Main class displays the current version of Scheduling
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public final class Main {

    public static String version = "{scheduler-version-main}";

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (version.equals("{scheduler-ver" + "sion-main}")) {//avoid pattern replacement here
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            version = sdf.format(d) + " - (Unreleased Version)";
        } else {
            version = "v" + version;
        }
        System.out.println("ProActive-Scheduling " + version);
    }

}
