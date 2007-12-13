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
package org.objectweb.proactive.core.security.crypto;

import java.security.SecureRandom;


public class RandomLongGenerator {
    private byte[] seed;
    private SecureRandom secureRandom;

    public RandomLongGenerator() {
        secureRandom = new SecureRandom();
    }

    public long generateLong(int nbBytes) {
        if (nbBytes > 8) {
            nbBytes = 8;
        }

        seed = new byte[nbBytes];
        seed = secureRandom.generateSeed(nbBytes);

        long ra2 = 0;

        for (int i = 0; i < 4; i++) {
            ra2 = ra2 +
                ((Math.abs(new Byte(seed[i]).longValue())) * new Double(Math.pow(10, (-3 + (3 * (i + 1)))))
                        .longValue());
        }

        return ra2;
    }
}
