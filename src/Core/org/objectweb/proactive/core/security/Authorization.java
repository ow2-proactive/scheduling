/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.security;

public enum Authorization {DENIED(-1),
    OPTIONAL(0),
    REQUIRED(1);
    private final int value;

    private Authorization(int value) {
        this.value = value;
    }

    public static Authorization compute(Authorization local,
        Authorization distant) throws IncompatiblePolicyException {
        return local.compute(distant);
    }

    public Authorization compute(Authorization that)
        throws IncompatiblePolicyException {
        if ((this.value * that.value) == -1) {
            throw new IncompatiblePolicyException("incompatible policies");
        }
        return realValue(this.value + that.value);
    }

    private Authorization realValue(int value) {
        if (value > 0) {
            return REQUIRED;
        }
        if (value < 0) {
            return DENIED;
        }
        return OPTIONAL;
    }

    public static Authorization fromString(String string) {
        for (Authorization value : Authorization.values()) {
            if (value.toString().equalsIgnoreCase(string)) {
                return value;
            }
        }
        return Authorization.DENIED;
    }
}
