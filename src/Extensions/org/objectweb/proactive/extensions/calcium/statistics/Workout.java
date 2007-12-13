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
package org.objectweb.proactive.extensions.calcium.statistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.extensions.calcium.muscle.Condition;
import org.objectweb.proactive.extensions.calcium.muscle.Conquer;
import org.objectweb.proactive.extensions.calcium.muscle.Divide;
import org.objectweb.proactive.extensions.calcium.muscle.Execute;
import org.objectweb.proactive.extensions.calcium.muscle.Muscle;


public class Workout implements Serializable {
    public HashMap<Class<?>, Exercise> muscleWorkout;
    public static ClassSorterByName classSorterByName = new ClassSorterByName();

    public Workout(int initHashSize) {
        muscleWorkout = new HashMap<Class<?>, Exercise>();
    }

    @Override
    public String toString() {
        String workout = "Workout: ";

        List<Class<?>> keys = new ArrayList<Class<?>>(muscleWorkout.keySet());
        Collections.sort(keys, classSorterByName);

        for (Class<?> muscle : keys) {
            workout += (muscle.getSimpleName() + "(" + muscleWorkout.get(muscle) + ") ");
        }

        return workout;
    }

    //TODO this method should not be public
    public void track(Muscle muscle, Timer timer) {
        if (!muscleWorkout.containsKey(muscle.getClass())) {
            muscleWorkout.put(muscle.getClass(), new Exercise(muscle.getClass()));
        }

        Exercise workout = muscleWorkout.get(muscle.getClass());
        workout.incrementComputationTime(timer);
    }

    protected void track(Workout workout) {
        java.util.Iterator<Class<?>> it = workout.muscleWorkout.keySet().iterator();
        while (it.hasNext()) {
            Class<?> muscle = it.next();
            if (!this.muscleWorkout.containsKey(muscle)) {
                this.muscleWorkout.put(muscle, new Exercise(muscle.getClass()));
            }
            Exercise exercise = this.muscleWorkout.get(muscle);
            exercise.incrementComputationTime(workout.muscleWorkout.get(muscle));
        }
    }

    public Exercise getExercise(Muscle muscle) {
        return muscleWorkout.get(muscle.getClass());
    }

    /**
     * Looks inside the workout for classes that implement the requested interface.
     * @param search The interface used as pattern.
     * @return The Exercise found for the Classes that implement the interface.
     */
    private List<Exercise> getExercises(Class<?> search) {
        Vector<Exercise> v = new Vector<Exercise>();

        java.util.Iterator<Class<?>> it = muscleWorkout.keySet().iterator();
        while (it.hasNext()) {
            Class<?> muscle = it.next();
            Class<?>[] interfaces = muscle.getInterfaces();
            for (Class<?> c : interfaces) {
                if (c.equals(search)) {
                    v.add(muscleWorkout.get(muscle));
                }
            }
        }

        return v;
    }

    public List<Exercise> getConditionExercises() {
        return getExercises(Condition.class);
    }

    public List<Exercise> getDivideExercises() {
        return getExercises(Divide.class);
    }

    public List<Exercise> getConquerExercise() {
        return getExercises(Conquer.class);
    }

    public List<Exercise> getExecuteExercise() {
        return getExercises(Execute.class);
    }

    static class ClassSorterByName implements Comparator<Class<?>> {
        public int compare(Class<?> o1, Class<?> o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
