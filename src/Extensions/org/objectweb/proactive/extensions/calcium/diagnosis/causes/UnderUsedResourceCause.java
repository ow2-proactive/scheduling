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
import java.util.Collections;
import java.util.List;

import org.objectweb.proactive.extensions.calcium.muscle.Divide;
import org.objectweb.proactive.extensions.calcium.statistics.Exercise;
import org.objectweb.proactive.extensions.calcium.statistics.Workout;


public class UnderUsedResourceCause extends AbstractCause {
    public String getDescription() {
        return "Not enough subtasks have been generated to use all available resources";
    }

    @Override
    protected String getMethodSearchString() {
        Method[] m = Divide.class.getMethods();
        return m[0].getName();
    }

    @Override
    protected List<Exercise> getSortedExcercise(Workout s) {
        List<Exercise> ex = s.getDivideExercises();
        Collections.sort(ex, Exercise.compareByInvokedTimes);
        return ex;
    }

    public boolean canBlameCode(Workout s) {
        return s.getDivideExercises().size() > 0;
    }

    public String suggestAction() {
        return "Method should divide the parameter into more parts.";
    }
}
