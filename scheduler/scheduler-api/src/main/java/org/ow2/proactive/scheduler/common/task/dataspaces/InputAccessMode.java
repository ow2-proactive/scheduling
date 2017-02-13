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

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * InputAccessMode provide a way to define how files should be accessed
 * in the executable.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.1
 */
@PublicAPI
public enum InputAccessMode {
    /** Transfer files from INPUT space to LOCAL space */
    TransferFromInputSpace("transferFromInputSpace"),
    /** Transfer files from OUTPUT space to LOCAL space */
    TransferFromOutputSpace("transferFromOutputSpace"),
    /**
     * Transfer files from GLOBAL space to LOCAL space
     */
    TransferFromGlobalSpace("transferFromGlobalSpace"),
    /** Transfer files from USER space to LOCAL space */
    TransferFromUserSpace("transferFromUserSpace"),
    /**
     * cache files from INPUT space to CACHE space
     */
    CacheFromInputSpace("cacheFromInputSpace"),
    /**
     * cache files from OUTPUT space to CACHE space
     */
    CacheFromOutputSpace("cacheFromOutputSpace"),
    /**
     * cache files from GLOBAL space to CACHE space
     */
    CacheFromGlobalSpace("cacheFromGlobalSpace"),
    /**
     * cache files from GLOBAL space to CACHE space
     */
    CacheFromUserSpace("cacheFromUserSpace"),
    /** Do nothing */
    none("none");

    String title;

    private InputAccessMode(String s) {
        title = s;
    }

    public static InputAccessMode getAccessMode(String accessMode) {
        if (TransferFromInputSpace.title.equalsIgnoreCase(accessMode)) {
            return TransferFromInputSpace;
        } else if (TransferFromOutputSpace.title.equalsIgnoreCase(accessMode)) {
            return TransferFromOutputSpace;
        } else if (TransferFromGlobalSpace.title.equalsIgnoreCase(accessMode)) {
            return TransferFromGlobalSpace;
        } else if (TransferFromUserSpace.title.equalsIgnoreCase(accessMode)) {
            return TransferFromUserSpace;
        } else if (CacheFromInputSpace.title.equalsIgnoreCase(accessMode)) {
            return CacheFromInputSpace;
        } else if (CacheFromOutputSpace.title.equalsIgnoreCase(accessMode)) {
            return CacheFromOutputSpace;
        } else if (CacheFromGlobalSpace.title.equalsIgnoreCase(accessMode)) {
            return CacheFromGlobalSpace;
        } else if (CacheFromUserSpace.title.equalsIgnoreCase(accessMode)) {
            return CacheFromUserSpace;
        } else if (none.title.equalsIgnoreCase(accessMode)) {
            return none;
        } else {
            throw new IllegalArgumentException("Unknow input access mode : " + accessMode);
        }
    }

    public String toString() {
        return this.title;
    }
}
