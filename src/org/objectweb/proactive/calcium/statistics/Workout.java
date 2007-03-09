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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.calcium.statistics;

import java.io.Serializable;
import java.util.HashMap;

import org.objectweb.proactive.calcium.muscle.Muscle;


public class Workout implements Serializable {
    private HashMap<Class, Exercise> muscleWorkout;

    Workout(int initHashSize) {
        muscleWorkout = new HashMap<Class, Exercise>(initHashSize);
    }

    @Override
    public String toString() {
        String workout = "Workout: ";
        java.util.Iterator<Class> it = muscleWorkout.keySet().iterator();
        while (it.hasNext()) {
            Class muscle = it.next();
            workout += (muscle.getSimpleName() + "(" +
            muscleWorkout.get(muscle) + ") ");
        }

        return workout;
    }

    //TODO this method should not be public
    public void track(Muscle muscle, Timer timer) {
        if (!muscleWorkout.containsKey(muscle.getClass())) {
            muscleWorkout.put(muscle.getClass(), new Exercise());
        }

        Exercise workout = muscleWorkout.get(muscle.getClass());
        workout.incrementComputationTime(timer);
    }

    protected void track(Workout workout) {
        java.util.Iterator<Class> it = workout.muscleWorkout.keySet().iterator();
        while (it.hasNext()) {
            Class muscle = it.next();
            if (!this.muscleWorkout.containsKey(muscle)) {
                this.muscleWorkout.put(muscle, new Exercise());
            }
            Exercise exercise = this.muscleWorkout.get(muscle);
            exercise.incrementComputationTime(workout.muscleWorkout.get(muscle));
        }
    }

    public Exercise getWorkout(Muscle muscle) {
        return muscleWorkout.get(muscle.getClass());
    }
}
