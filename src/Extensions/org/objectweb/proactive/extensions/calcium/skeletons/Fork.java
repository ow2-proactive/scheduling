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
package org.objectweb.proactive.extensions.calcium.skeletons;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.calcium.muscle.Conquer;
import org.objectweb.proactive.extensions.calcium.muscle.Divide;
import org.objectweb.proactive.extensions.calcium.muscle.Execute;
import org.objectweb.proactive.extensions.calcium.stateness.StateFul;
import org.objectweb.proactive.extensions.calcium.stateness.Stateness;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystem;


/**
 * This skeleton represents data parallelism: multiple data multiple instructions (MIMD).
 *
 * By default, a parameter inputed into this {@link Skeleton} is copied, and each sub-parameter is then
 * inputed into a different nested {@link Skeleton}. (MISD: Multiple Instruction Single Data)
 *
 * Additionally, a divide method can be provided to divide the parameter into sub-parameters.
 * Each sub-parameter will be inputed into a different nested {@link Skeleton}. (MIMD)
 *
 * The results of the nested {@link Skeleton}s will be consolidated into a single one using
 * the specified {@ Conquer} object.
 *
 * @author The ProActive Team (mleyton)
 */
@PublicAPI
public class Fork<P extends java.io.Serializable, R extends java.io.Serializable>
    implements Skeleton<P, R> {
    Divide<P, ?> div;
    Conquer<?, R> conq;
    List<Skeleton> subSkelList;

    /**
     * This constructor uses the <code>Fork</code>'s default {@link Divide} and {@link Conquer}.
     *
     * The default {@link Divide} behavior is to copy the input parameter.
     * The default {@link Conquer} behavior is to return the first result of the array.
     *
     * @see org.objectweb.proactive.extensions.calcium.skeletons.Fork.ForkDefaultDivide
     * @see org.objectweb.proactive.extensions.calcium.skeletons.Fork.ForkDefaultConquer
     *
     * @param args
     *            The nested {@link Skeleton}s that will be executed, in parallel, on one of the parameter copies.
     */
    public Fork(Skeleton<P, R>... args) {
        forkInit(new ForkDefaultDivide<P>(args.length), Arrays.asList(args),
            new ForkDefaultConquer<R>());
    }

    /**
     * This constructor uses the <code>Fork</code>'s default {@link Divide} and {@link Conquer}.
     * It wraps the {@link Execute} parameters into a {@link Seq} {@link Skeleton}, and invokes
     * the default constructor {@link #Fork(Execute...)}.
     *
     * @see org.objectweb.proactive.extensions.calcium.skeletons.Fork.ForkDefaultDivide
     * @see org.objectweb.proactive.extensions.calcium.skeletons.Fork.ForkDefaultConquer
     *
     * @param args
     *            The {@link Execute}s that will be executed, in parallel, on one of the parameter copies.
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
     * This constructor uses a default {@link Divide} object, and
     * uses the customized {@link Conquer} object provided as parameter to reduce the results.
     *
     * @see org.objectweb.proactive.extensions.calcium.skeletons.Fork.ForkDefaultDivide
     *
     * @param conq
     *            Reduces the computed results into a single one.
     * @param args
     *            The nested {@link Skeleton}s that will be executed, in parallel, on one of the parameter copies.
     */
    public <X extends Serializable>Fork(Conquer<X, R> conq,
        Skeleton<P, X>... args) {
        forkInit(new ForkDefaultDivide<P>(args.length), Arrays.asList(args),
            conq);
    }

    /**
     * This constructor allows for a customized {@link Divide} and {@link Conquer} skeletons.
     *
     * The number of elements returned by the {@link Divide} object must be the same as
     * the number of <code>skelList</code>, or a MuscleException error will be generated at runtime.
     *
     * @param div
     *            The custom {@link Divide} object.
     * @param args
     *            The nested {@link Skeleton}s that will be executed, in parallel, on one of the parameter copies.
     * @param conq
     *            Reduces the computed results into a single one.
     */
    public <X extends Serializable, Y extends Serializable>Fork(
        Conquer<Y, R> conq, Divide<P, X> div, Skeleton<X, Y>... args) {
        forkInit(div, Arrays.asList(args), conq);
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

    /**
     * @see Skeleton#accept(SkeletonVisitor)
     */
    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "Fork";
    }

    /**
     * This is the default {@link Divide} behavor for <code>Fork</code>. It deep-copies the input
     * parameter into N copies.
     *
     * The copy corresponds to a deep-copy, ie each nested skeleton receives a copy of the
     * parameter. The deep-copy can be inefficient (because relies in serialization).
     *
     * @author The ProActive Team (mleyton)
     */
    @StateFul(value = false)
    static public class ForkDefaultDivide<T> implements Divide<T, T> {
        int number;

        /**
         * The constructor specifying the number of copies to generate.
         *
         * @param number The number of copies.
         */
        public ForkDefaultDivide(int number) {
            this.number = number;
        }

        /**
         * Divides the parameter into <number> copies.
         * @throws ClassNotFoundException
         * @throws IOException
         *
         * @see Divide#divide(SkeletonSystem, Object)
         */
        public List<T> divide(SkeletonSystem system, T param)
            throws IOException, ClassNotFoundException {
            Vector<T> vector;

            vector = Stateness.deepCopy(param, number);

            return vector;
        }
    }

    /**
     * This class is the default {@link Conquer} method for <code>Fork</code>.
     *
     * The reduction of results simply chooses the first result of the array.
     *
     * @author The ProActive Team (mleyton)
     *
     */
    static public class ForkDefaultConquer<T> implements Conquer<T, T> {

        /**
             * @see Conquer#conquer(SkeletonSystem, Object[])
             */
        public T conquer(SkeletonSystem system, T[] param) {
            return param[0];
        }
    }
}
