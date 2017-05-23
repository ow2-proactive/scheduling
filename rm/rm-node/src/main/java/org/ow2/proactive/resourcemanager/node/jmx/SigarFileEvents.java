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
package org.ow2.proactive.resourcemanager.node.jmx;

import java.io.File;
import java.io.FilenameFilter;

import org.hyperic.sigar.SigarException;


/**
 * Created by brian on 21/05/2017.
 */
public class SigarFileEvents implements SigarFileEventsMXBean {
    private String path;

    private String suffix;

    public SigarFileEvents(String monitor) {
        String[] monitorArray = monitor.split("#");
        this.path = monitorArray[0];
        if (monitorArray.length > 1) {
            this.suffix = monitorArray[1];
        } else {
            this.suffix = null;
        }
    }

    @Override
    public int getFileCount() throws SigarException {
        if (suffix == null) {
            return new File(path).listFiles().length;
        } else {
            return new File(path).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(suffix);
                }
            }).length;
        }
    }
}
