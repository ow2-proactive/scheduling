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
package org.objectweb.proactive.extensions.calcium.skeletons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.calcium.exceptions.EnvironmentException;
import org.objectweb.proactive.extensions.calcium.muscle.Conquer;
import org.objectweb.proactive.extensions.calcium.muscle.Divide;
import org.objectweb.proactive.extensions.calcium.muscle.Execute;
import org.objectweb.proactive.extensions.calcium.stateness.StateFul;
import org.objectweb.proactive.extensions.calcium.stateness.Stateness;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystem;


/**
 * This skeleton represents MDMI parallelism. The parameter recieved by this
 * skeleton will be divided and passed to it's sub skeleton instructions.
 *
 * The reduction of the results will be performed by the Conquer objects
 * required as a parameters.
 *
 * @author The ProActive Team (mleyton)
 *
 * @param
 * <P>
 */
@PublicAPI
public class Fork<P extends java.io.Serializable, R extends java.io.Serializable>
    implements Skeleton<P, R> {
    Divide<P, ?> div;
    Conquer<?, R> conq;
    List<Skeleton> subSkelList;

    /**
     * This constructor uses the Fork's default Divide and Conquer.
     *
     * @see org.objectweb.proactive.extensions.calcium.skeletons.Fork.ForkDefaultDivide
     * @see org.objectweb.proactive.extensions.calcium.skeletons.Fork.ForkDefaultConquer
     *
     * @param args
     *            The multiple instructions (skeletons) that will be executed on
     *            each copy of the parameter
     *            <P>
     */
    public Fork(Skeleton<P, R>... args) {
        forkInit(new ForkDefaultDivide<P>(args.length), Arrays.asList(args),
            new ForkDefaultConquer<R>());
    }

    /**
     * This constructor uses the Fork's default Divide and Conquer.
     *
     * @see org.objectweb.proactive.extensions.calcium.skeletons.Fork.ForkDefaultDivide
     * @see org.objectweb.proactive.extensions.calcium.skeletons.Fork.ForkDefaultConquer
     *
     * @param args
     *            The multiple instructions that will be executed on each copy
     *            of the parameter
     *            <P>
     */
    public Fork(Execute<P, R>... args) {
        ArrayList<Skeleton<P, R>> list = new ArrayList<Skeleton<P, R>>();
        for (Execute<P, R> e : args) {
            list.add(new Seq<P, R>(e));
        }

        forkInit(new ForkDefaultDivide<P>(args.length), list,
            new ForkDefaultConquer<R>());
    }

    /**
     * Creates a Fork skeleton structure. The default divition of the parameters
     * corresponds to a deep copy, ie each sub-skeleton recieves a copy of the
     * parameter. If the deepcopy is inefficiente (because it uses
     * serialization), use the alternative constructure and provide a custom
     * Divide method.
     *
     * @see org.objectweb.proactive.extensions.calcium.skeletons.Fork.ForkDefaultDivide
     *
     * @param conq
     *            Conqueres the computed results into a single one.
     * @param conq
     *            The conquering (reduction) used to consolidate the results of
     *            the sub-skeletons into a single result.
     * @param stages
     *            The sub-skeletons that can be computed in parallel.
     */
    public <X extends Serializable>Fork(Conquer<X, R> conq,
        Skeleton<P, X>... args) {
        forkInit(new ForkDefaultDivide<P>(args.length), Arrays.asList(args),
            conq);
    }

    /**
     * Creates a Fork skeleton structure, allowing a custom divide method. The
     * number of elemenents returned by the divide method must be the same as
     * the number of stages, or a MuscleException error will be generated at
     * runtime.
     *
     * @param div
     *            The custom divide method.
     * @param skelList
     *            The sub-skeletons that can be computed in parallel.
     * @param conq
     *            The conquering (reduction) used to consolidate the results of
     *            the sub-skeletons into a single result.
     */
    public <X extends Serializable, Y extends Serializable>Fork(
        Conquer<Y, R> conq, Divide<P, X> div, Skeleton<X, Y>... skelList) {
        forkInit(div, Arrays.asList(skelList), conq);
    }

    private <X extends Serializable, Y extends Serializable> void forkInit(
        Divide<P, X> div, List<Skeleton<X, Y>> skelList, Conquer<Y, R> conq) {
        if (skelList.size() <= 0) {
            throw new IllegalArgumentException(
                "Fork must have at least one instruction");
        }

        this.div = div;
        this.conq = conq;
        this.subSkelList = new ArrayList<Skeleton>();
        for (Skeleton<X, Y> skel : skelList) {
            this.subSkelList.add(skel);
        }
    }

    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "Fork";
    }

    /**
     * This is the default divide behaviour for Fork. It simply deep copies the
     * parameters N times.
     *
     * @author The ProActive Team (mleyton)
     */
    @StateFul(value = false)
    static public class ForkDefaultDivide<T> implements Divide<T, T> {
        int number;

        public ForkDefaultDivide(int number) {
            this.number = number;
        }

        public Vector<T> divide(SkeletonSystem system, T param)
            throws EnvironmentException {
            Vector<T> vector;
            try {
                vector = Stateness.deepCopy(param, number);
            } catch (Exception e) {
                logger.error("Unable to make a deep copy:" + e.getMessage());
                throw new EnvironmentException(e);
            }
            return vector;
        }
    }

    /**
     * This class is the default Conquer method for Fork. To reduce the results
     * of the computation, this divide simply chooses a result parameter at
     * random.
     *
     * @author The ProActive Team (mleyton)
     *
     * @param <T>
     */
    static public class ForkDefaultConquer<T> implements Conquer<T, T> {
        public T conquer(SkeletonSystem system, T[] param)
            throws RuntimeException, EnvironmentException {
            return param[0];
        }
    }
}
