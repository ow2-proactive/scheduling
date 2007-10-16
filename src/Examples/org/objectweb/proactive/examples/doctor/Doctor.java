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
package org.objectweb.proactive.examples.doctor;

public class Doctor {
    int id;
    RandomTime rand;
    Office off;
    long meanDur;
    long sigmaDur;

    public Doctor() {
    }

    public Doctor(Integer _id, Long _meanDur, Long _sigmaDur, Office _off,
        RandomTime _rand) {
        id = _id.intValue();
        meanDur = _meanDur.longValue();
        sigmaDur = _sigmaDur.longValue();
        off = _off;
        rand = _rand;
    }

    public void curePatient(int _pat) {
        long temps = rand.gaussianTime(meanDur, sigmaDur);

        try {
            Thread.sleep(temps);
        } catch (InterruptedException e) {
        }

        off.doctorCureFound(id, _pat, new Cure());
    }
}
