/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
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

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.objectweb.proactive.annotation.PublicAPI;

@PublicAPI
@XmlRootElement(name = "onTaskError")
@XmlAccessorType(XmlAccessType.FIELD)
public class OnTaskError implements Serializable {

    // PUBLIC AND PRIVATE CONSTANTS
    private static final String CANCEL_JOB_STRING = "cancelJob";
    public static final OnTaskError CANCEL_JOB = new OnTaskError(CANCEL_JOB_STRING);
    private static final String SUSPEND_TASK_STRING = "suspendTask";
    public static final OnTaskError PAUSE_TASK = new OnTaskError(SUSPEND_TASK_STRING);
    private static final String PAUSE_JOB_STRING = "pauseJob";
    public static final OnTaskError PAUSE_JOB = new OnTaskError(PAUSE_JOB_STRING);
    private static final String CONTINUE_JOB_EXECUTION_STRING = "continueJobExecution";
    public static final OnTaskError CONTINUE_JOB_EXECUTION = new OnTaskError(CONTINUE_JOB_EXECUTION_STRING);
    private static final String NONE_STRING = "none";
    public static final OnTaskError NONE = new OnTaskError(NONE_STRING);
    // Member
    @XmlAttribute
    private final String descriptor;


    private OnTaskError(String descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * Get a OnTaskError instance based on a descriptor string. If the descriptor string is not found,
     * 'none' is returned.
     * @param descriptor Descriptor string.
     * @return OnTaskError instance or 'not set' if descriptor string is not recognized.
     */
    public static OnTaskError getInstance(String descriptor) {
        switch (descriptor) {
            case CANCEL_JOB_STRING:
                return CANCEL_JOB;
            case SUSPEND_TASK_STRING:
                return PAUSE_TASK;
            case PAUSE_JOB_STRING:
                return PAUSE_JOB;
            case CONTINUE_JOB_EXECUTION_STRING:
                return CONTINUE_JOB_EXECUTION;
            default:
                return NONE;
        }
    }

    @Override
    public String toString() {
        return this.descriptor;
    }

    @Override
    public boolean equals(Object onTaskError) {
        if (onTaskError == null) {
            return false;
        }
        if (onTaskError == this) {
            return true;
        }
        if (onTaskError.getClass() != getClass()) {
            return this.toString().equals(onTaskError.toString());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.descriptor.hashCode();
    }
}
