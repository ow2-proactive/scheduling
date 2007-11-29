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

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.calcium.muscle.Execute;


/**
 * The <code>Pipe</code> {@link Skeleton} represents staged computation.
 *
 * Each element of the pipe will be executed sequentially for each {@link Skeleton} element.
 *
 * @author The ProActive Team (mleyton)
 */
@PublicAPI
public class Pipe<P extends java.io.Serializable, R extends java.io.Serializable>
    implements Skeleton<P, R> {
    Vector<Skeleton<?, ?>> stages;

    /**
     * The basic constructor. Will execute <code>stage1</code> and the result of
     * <code>stage1</code> will be passed as parameter to <code>stage2</code>
         *
     * @param stage1 The first stage to execute.
     * @param stage2 The second stage to execute.
     */
    public <X extends Serializable>Pipe(Skeleton<P, X> stage1,
        Skeleton<X, R> stage2) {
        stages = new Vector<Skeleton<?, ?>>();

        stages.add(stage1);
        stages.add(stage2);
    }

    /**
     * Like the {@link Pipe#Pipe(Skeleton, Skeleton) basic} constructor, but accepts
     * an {@link Execute} instead of a {@link Skeleton}.
     *
     * Each {@link Execute} will be wrapped in a {@link Seq} {@link Skeleton}.
     *
     * @param stage1 The first stage of computation.
     * @param stage2 The second stage of computation.
     */
    public <X extends Serializable>Pipe(Execute<P, X> stage1,
        Execute<X, R> stage2) {
        stages = new Vector<Skeleton<?, ?>>();

        stages.add(new Seq<P, X>(stage1));
        stages.add(new Seq<X, R>(stage2));
    }

    /**
     * Like the {@link Pipe#Pipe(Skeleton, Skeleton) basic} constructor, but accepts
     * three stages instead of two.
     *
     * @param stage1 The first stage of computation.
     * @param stage2 The second stage of computation.
     * @param child3 The third stage of computation.
     */
    public <X extends Serializable, Y extends Serializable>Pipe(
        Skeleton<P, X> child1, Skeleton<X, Y> child2, Skeleton<Y, R> child3) {
        stages = new Vector<Skeleton<?, ?>>();

        stages.add(child1);
        stages.add(child2);
        stages.add(child3);
    }

    /**
     * Like the {@link Pipe#Pipe(Skeleton, Skeleton) basic} constructor, but accepts
     * three stages of {@link Execute} instead of two.
     *
     * Each {@link Execute} will be wrapped in a {@link Seq} {@link Skeleton}.
     *
     * @param stage1 The first stage of computation.
     * @param stage2 The second stage of computation.
     * @param child3 The third stage of computation.
     */
    public <X extends Serializable, Y extends Serializable>Pipe(
        Execute<P, X> child1, Execute<X, Y> child2, Execute<Y, R> child3) {
        stages = new Vector<Skeleton<?, ?>>();

        stages.add(new Seq<P, X>(child1));
        stages.add(new Seq<X, Y>(child2));
        stages.add(new Seq<Y, R>(child3));
    }

    /**
     * Like the {@link Pipe#Pipe(Skeleton, Skeleton) basic} constructor, but accepts
     * four stages instead of two.
     *
     * For building <code>Pipe</code>s with more than four stages, a <code>Pipe</code> nesting other <code>Pipe</code>'s can be used.
     *
     * @param stage1 The first stage of computation.
     * @param stage2 The second stage of computation.
     * @param child3 The third stage of computation.
     * @param child4 The third stage of computation.
     */
    public <X extends Serializable, Y extends Serializable, Z extends Serializable>Pipe(
        Skeleton<P, X> child1, Skeleton<X, Y> child2, Skeleton<Y, Z> child3,
        Skeleton<Z, R> child4) {
        stages = new Vector<Skeleton<?, ?>>();

        stages.add(child1);
        stages.add(child2);
        stages.add(child3);
        stages.add(child4);
    }

    /**
     * Like the {@link Pipe#Pipe(Skeleton, Skeleton) basic} constructor, but accepts
     * four stages of {@link Execute} instead of two.
     *
     * Each {@link Execute} will be wrapped in a {@link Seq} {@link Skeleton}.
     *
     * @param stage1 The first stage of computation.
     * @param stage2 The second stage of computation.
     * @param child3 The third stage of computation.
     * @param child4 The third stage of computation.
     */
    public <X extends Serializable, Y extends Serializable, Z extends Serializable>Pipe(
        Execute<P, X> child1, Execute<X, Y> child2, Execute<Y, Z> child3,
        Execute<Z, R> child4) {
        stages = new Vector<Skeleton<?, ?>>();

        stages.add(new Seq<P, X>(child1));
        stages.add(new Seq<X, Y>(child2));
        stages.add(new Seq<Y, Z>(child3));
        stages.add(new Seq<Z, R>(child4));
    }

    /**
     * @see Skeleton#accept(SkeletonVisitor)
     */
    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }
}
