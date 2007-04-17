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
package org.objectweb.proactive.extensions.calcium.diagnosis;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.diagnosis.causes.*;
import org.objectweb.proactive.extensions.calcium.diagnosis.inferences.*;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.*;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.bt1.*;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.bt2.*;
import org.objectweb.proactive.extensions.calcium.skeletons.Fork;
import org.objectweb.proactive.extensions.calcium.statistics.Exercise;
import org.objectweb.proactive.extensions.calcium.statistics.Stats;
import org.objectweb.proactive.extensions.calcium.statistics.StatsImpl;
import org.objectweb.proactive.extensions.calcium.statistics.Timer;
import org.objectweb.proactive.extensions.calcium.statistics.Workout;


public class Efficiency {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_DIAGNOSIS);
    Vector<Inference> inference;

    Efficiency() {
        inference = new Vector<Inference>();

        inference.add(new CoarseGranularity(10, new UnderusedResources(0.8),
                new LastTaskPenalty(0.1)));

        inference.add(new FineGranularity(1, new DeepTree(3), new WideTree(20)));
    }

    List<Cause> getCauses(Stats stats) {
        List<Cause> causes = new Vector<Cause>();
        for (Inference inf : inference) {
            causes.addAll(inf.getCauses(stats));
        }

        return causes;
    }

    public static void main(String[] args) {

        /*
        StatsImpl stats16 = new StatsImpl();
        StatsImpl stats17 = new StatsImpl();
        StatsImpl stats18 = new StatsImpl();

        stats16.computationTime=14726;
        stats16.waitingTime=304;
        stats16.processingTime=128883;
        stats16.readyTime=1218135;
        stats16.resultsTime=0;
        stats16.initTime=0;
        stats16.finitTime=1305;
        stats16.currentStateStart=0;
        stats16.workout=null;
        stats16.maxResources=100;
        stats16.subTreeSize=24892;
        stats16.numberLeafs=22679;

        Workout w18 = new Workout(7);
        w18.muscleWorkout.put(DivideBT1.class, new Exercise(DivideBT1.class, 19, 259));
        w18.muscleWorkout.put(DivideBT2.class, new Exercise(DivideBT2.class, 154, 1918));
        w18.muscleWorkout.put(Fork.ForkDefaultDivide.class, new Exercise(Fork.ForkDefaultDivide.class, 1, 2));
        w18.muscleWorkout.put(SolveBT2.class, new Exercise(SolveBT2.class, 19872, 13619957));
        w18.muscleWorkout.put(ConquerBoard.class, new Exercise(ConquerBoard.class, 2178, 175));
        w18.muscleWorkout.put(SolveBT1.class, new Exercise(SolveBT1.class, 2842, 1103186));
        w18.muscleWorkout.put(DivideCondition.class, new Exercise(DivideCondition.class, 24891, 523));

        stats17.computationTime=14816;
        stats17.waitingTime=196;
        stats17.processingTime=17593;
        stats17.readyTime=46771;
        stats17.resultsTime=0;
        stats17.initTime=0;
        stats17.finitTime=197;
        stats17.currentStateStart=0;
        stats17.workout=null;
        stats17.maxResources=100;
        stats17.subTreeSize=2178;
        stats17.numberLeafs=2010;

        Workout w17 = new Workout(7);
        w17.muscleWorkout.put(DivideBT1.class, new Exercise(DivideBT1.class, 19, 259));
        w17.muscleWorkout.put(DivideBT2.class, new Exercise(DivideBT2.class, 154, 1918));
        w17.muscleWorkout.put(Fork.ForkDefaultDivide.class, new Exercise(Fork.ForkDefaultDivide.class, 1, 2));
        w17.muscleWorkout.put(SolveBT2.class, new Exercise(SolveBT2.class, 19872, 13619957));
        w17.muscleWorkout.put(ConquerBoard.class, new Exercise(ConquerBoard.class, 2178, 175));
        w17.muscleWorkout.put(SolveBT1.class, new Exercise(SolveBT1.class, 2842, 1103186));
        w17.muscleWorkout.put(DivideCondition.class, new Exercise(DivideCondition.class, 24891, 523));


        stats18.computationTime=15105;
        stats18.waitingTime=267;
        stats18.processingTime=15360;
        stats18.readyTime=2003;
        stats18.resultsTime=0;
        stats18.initTime=0;
        stats18.finitTime=268;
        stats18.currentStateStart=0;
        stats18.workout=null;
        stats18.maxResources=100;
        stats18.subTreeSize=166;
        stats18.numberLeafs=153;

        Workout w16 = new Workout(7);
        w16.muscleWorkout.put(DivideBT1.class, new Exercise(DivideBT1.class, 19, 259));
        w16.muscleWorkout.put(DivideBT2.class, new Exercise(DivideBT2.class, 154, 1918));
        w16.muscleWorkout.put(Fork.ForkDefaultDivide.class, new Exercise(Fork.ForkDefaultDivide.class, 1, 2));
        w16.muscleWorkout.put(SolveBT2.class, new Exercise(SolveBT2.class, 19872, 13619957));
        w16.muscleWorkout.put(ConquerBoard.class, new Exercise(ConquerBoard.class, 2178, 175));
        w16.muscleWorkout.put(SolveBT1.class, new Exercise(SolveBT1.class, 2842, 1103186));
        w16.muscleWorkout.put(DivideCondition.class, new Exercise(DivideCondition.class, 24891, 523));


        Efficiency eff = new Efficiency();

        List<Cause> causes = eff.getCauses(stats16);
        for(Cause c:causes){
                System.out.println("Cause:"+c.getDescription());
                System.out.println("Action:"+c.suggestAction());
                List<Method> blamed= c.blameCode(w18);
                for(Method m:blamed ){
                        System.out.println(m);
                }

        }
        System.out.println("done");
        */
    }
}
