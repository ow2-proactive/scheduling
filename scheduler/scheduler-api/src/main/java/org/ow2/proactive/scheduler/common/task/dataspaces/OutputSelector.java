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
package org.ow2.proactive.scheduler.common.task.dataspaces;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector;


/**
 * OutputSelector is a couple of {@link FileSelector} and {@link OutputAccessMode}
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
public class OutputSelector implements Serializable {

    private FileSelector outputFiles = null;

    private OutputAccessMode mode;

    public OutputSelector() {
    }

    /**
     * Create a new instance of OutputSelector
     *
     * @param outputFiles
     * @param mode
     */
    public OutputSelector(FileSelector outputFiles, OutputAccessMode mode) {
        this.outputFiles = outputFiles;
        this.mode = mode;
    }

    /**
     * Get the outputFiles
     *
     * @return the outputFiles
     */
    public FileSelector getOutputFiles() {
        return outputFiles;
    }

    /**
     * Set the outputFiles value to the given outputFiles value
     *
     * @param outputFiles the outputFiles to set
     */
    public void setOutputFiles(FileSelector outputFiles) {
        this.outputFiles = outputFiles;
    }

    /**
     * Get the mode
     *
     * @return the mode
     */
    public OutputAccessMode getMode() {
        return mode;
    }

    /**
     * Set the mode value to the given mode value
     *
     * @param mode the mode to set
     */
    public void setMode(OutputAccessMode mode) {
        this.mode = mode;
    }

    /**
     * Return a string representation of this selector.
     */
    public String toString() {
        return "(" + this.mode + "-" + this.outputFiles + ")";
    }

}
