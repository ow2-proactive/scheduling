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
package org.objectweb.proactive.calcium.skeletons;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.calcium.Task;
import org.objectweb.proactive.calcium.exceptions.EnvironmentException;
import org.objectweb.proactive.calcium.exceptions.MuscleException;
import org.objectweb.proactive.calcium.muscle.Conquer;
import org.objectweb.proactive.calcium.muscle.Divide;
import org.objectweb.proactive.calcium.statistics.Timer;


/**
 * This skeleton represents Parallelism (data parallelism).
 * The parameter recieved by this skeleton will be copied
 * and passed to it's sub skeletons.
 *
 * The reduction of the results will be performed by the
 * Conquer objects required as a parameter.
 *
 * @author The ProActive Team (mleyton)
 *
 * @param <P>
 */
public class Fork<P, R> implements Skeleton<P, R>, Instruction<P, P> {
    Divide<P, ?> div;
    Conquer<?, R> conq;
    Vector<Skeleton<?, ?>> stages;

    /**
     * Creates a Fork skeleton structure. The default divition of the parameters
     * corresponds to a deep copy, ie each sub-skeleton recieves a copy of the parameter.
     * If the deepcopy is inefficiente (because it uses serialization), use the alternative
     * constructure and provide a custom Divide method.
     *
     * @param conq Conqueres the computed results into a single one.
     * @param conq The conquering (reduction) used to consolidate the results
     * of the sub-skeletons into a single result.
     * @param stages The sub-skeletons that can be computed in parallel.         */
    public <X>Fork(Conquer<X, R> conq, Skeleton<P, X>... args) {
        this(null, conq, Arrays.asList(args));
        this.div = new ForkDefaultDivide<P>(args.length);
    }

    /**
     * Creates a Fork skeleton structure, allowing a custom divide method.
     * The number of elemenents returned by the divide method must be
     * the same as the number of stages, or a MuscleException error will be
     * generated at runtime.
     *
     * @param div The custom divide method.
     * @param conq The conquering (reduction) used to consolidate the results
     * of the sub-skeletons into a single result.
     * @param stages The sub-skeletons that can be computed in parallel.
     */
    public <X, Y>Fork(Divide<P, X> div, Conquer<Y, R> conq,
        List<Skeleton<X, Y>> stages) {
        if (stages.size() <= 0) {
            throw new IllegalArgumentException(
                "Fork must have at least one stage");
        }

        this.div = div;
        this.conq = conq;
        this.stages = new Vector<Skeleton<?, ?>>();
        this.stages.addAll(stages);
    }

    public Vector<Instruction<?, ?>> getInstructionStack() {
        Vector<Instruction<?, ?>> v = new Vector<Instruction<?, ?>>();
        v.add(this);

        return v;
    }

    public Task<P> compute(Task<P> t) throws EnvironmentException {
        t.pushInstruction(new DaC.ConquerInst(conq));
        t.pushInstruction(new DivideInst(div, stages));

        return t;
    }

    public String toString() {
        return "Fork";
    }

    /**
     * This is the default divide behaviour for Fork.
     * It simply deep copies the parameters N times.
     * @author The ProActive Team (mleyton)
     */
    static class ForkDefaultDivide<T> implements Divide<T, T> {
        int number;

        public ForkDefaultDivide(int number) {
            this.number = number;
        }

        public Vector<T> divide(T param) throws EnvironmentException {
            Vector<T> vector;
            try {
                vector = deepCopy(param, number);
            } catch (Exception e) {
                logger.error("Unable to make a deep copy:" + e.getMessage());
                throw new EnvironmentException(e.getMessage());
            }
            return vector;
        }

        @SuppressWarnings("unchecked")
        private Vector<T> deepCopy(T o, int n)
            throws IOException, ClassNotFoundException {
            // serialize Object into byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            byte[] buf = baos.toByteArray();
            oos.close();

            // deserialize byte array
            Vector<T> vector = new Vector<T>(n);
            while (n-- > 0) {
                ByteArrayInputStream bais = new ByteArrayInputStream(buf);
                ObjectInputStream ois = new ObjectInputStream(bais);
                vector.add((T) ois.readObject());
                ois.close();
            }

            return vector;
        }
    }

    static class DivideInst<T, X> implements Instruction<T, T> {
        private Divide<T, X> div;
        private Vector<Skeleton<T, ?>> stages;

        public DivideInst(Divide<T, X> div, Vector<Skeleton<T, ?>> stages) {
            this.div = div;
            this.stages = stages;
        }

        public Task<T> compute(Task<T> parent) throws Exception {
            Timer timer = new Timer();
            Vector<X> childObjects = div.divide(parent.getObject());
            timer.stop();

            if (childObjects.size() != stages.size()) {
                String msg = "Divided Parameter(" + childObjects.size() +
                    ") and number stages(" + stages.size() + ") don't match.";
                logger.error(msg);
                throw new MuscleException(msg);
            }

            for (int i = 0; i < stages.size(); i++) {
                Task<X> child = new Task<X>(childObjects.elementAt(i));
                child.setStack(stages.elementAt(i).getInstructionStack()); //Each child task executes a different sub-skeleton
                parent.addReadyChild(child); //parent remebers it's children
            }

            //Now we put this instruction to perform a conquer when returning 
            parent.getStats().getWorkout().track(div, timer);
            return parent;
        }

        public Task<?> computeUnknown(Task<?> t) throws Exception {
            return compute((Task<T>) t);
        }
    }

    public Task<?> computeUnknown(Task<?> t) throws Exception {
        return compute((Task<P>) t);
    }
}
