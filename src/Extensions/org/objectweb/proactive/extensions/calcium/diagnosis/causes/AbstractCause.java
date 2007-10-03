/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.calcium.diagnosis.causes;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.extensions.calcium.statistics.Exercise;
import org.objectweb.proactive.extensions.calcium.statistics.Workout;


public abstract class AbstractCause implements Cause {
    public List<Method> blameCode(Workout s) {
        return blamecode(s, DEFAULT_NUMBER_OF_CAUSES);
    }

    public List<Method> blamecode(Workout s, int number) {
        List<Method> blamed = new Vector<Method>();

        List<Exercise> ex = getSortedExcercise(s);

        for (int i = 0; i < Math.min(number, ex.size()); i++) {
            Class<?> c = ex.get(i).getMuscleClass();
            try {
                Method[] mall = c.getMethods();
                for (Method m : mall) {
                    if (!m.isBridge() &&
                            (m.getName() == getMethodSearchString())) {
                        blamed.add(m);
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error("Unable to find method: " +
                    getMethodSearchString() + " in class: " + c.getName());
            }
        }

        return blamed;
    }

    abstract protected List<Exercise> getSortedExcercise(Workout s);

    abstract protected String getMethodSearchString();
}
