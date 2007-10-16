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
import java.util.Stack;
import java.util.Vector;

import org.objectweb.proactive.extensions.calcium.instructions.DaCInst;
import org.objectweb.proactive.extensions.calcium.instructions.ForInst;
import org.objectweb.proactive.extensions.calcium.instructions.ForkInst;
import org.objectweb.proactive.extensions.calcium.instructions.IfInst;
import org.objectweb.proactive.extensions.calcium.instructions.Instruction;
import org.objectweb.proactive.extensions.calcium.instructions.MapInst;
import org.objectweb.proactive.extensions.calcium.instructions.SeqInst;
import org.objectweb.proactive.extensions.calcium.instructions.WhileInst;


/**
 * This class constructs an instruction stack based on the skeleton nested program.
 *
 * To build the instruction stack, the visitor pattern is used.
 *
 * @author The ProActive Team (mleyton)
 */
public class InstructionBuilderVisitor implements SkeletonVisitor {
    public Stack<Instruction> stack;

    public InstructionBuilderVisitor() {
        this.stack = new Stack<Instruction>();
    }

    public <P extends Serializable, R extends Serializable> void visit(
        Farm<P, R> farm) {
        farm.child.accept(this);
    }

    public <P extends Serializable, R extends Serializable> void visit(
        Pipe<P, R> pipe) {
        //the last instructions go into the stack first
        for (int i = pipe.stages.size() - 1; i >= 0; i--) {
            pipe.stages.get(i).accept(this);
        }
    }

    public <P extends Serializable, R extends Serializable> void visit(
        Seq<P, R> seq) {
        stack.add(new SeqInst<P, R>(seq.secCode));
    }

    public <P extends Serializable, R extends Serializable> void visit(
        If<P, R> ifskel) {
        //build substacks for both cases
        InstructionBuilderVisitor ifChildVisitor = new InstructionBuilderVisitor();
        InstructionBuilderVisitor elseChildVisitor = new InstructionBuilderVisitor();

        ifskel.ifChild.accept(ifChildVisitor);
        ifskel.elseChild.accept(elseChildVisitor);

        //stack a the new instruction holding both possible cases
        IfInst<P> ifinst = new IfInst<P>(ifskel.cond, ifChildVisitor.stack,
                elseChildVisitor.stack);
        stack.add(ifinst);
    }

    public <P extends Serializable> void visit(For<P> forSkel) {
        InstructionBuilderVisitor childVisitor = new InstructionBuilderVisitor();
        forSkel.child.accept(childVisitor);

        stack.add(new ForInst<P>(forSkel.times, childVisitor.stack));
    }

    public <P extends Serializable> void visit(While<P> whileSkel) {
        InstructionBuilderVisitor childVisitor = new InstructionBuilderVisitor();
        whileSkel.child.accept(childVisitor);

        stack.add(new WhileInst<P>(whileSkel.cond, childVisitor.stack));
    }

    public <P extends Serializable, R extends Serializable> void visit(
        Map<P, R> map) {
        InstructionBuilderVisitor childVisitor = new InstructionBuilderVisitor();
        map.child.accept(childVisitor);

        stack.add(new MapInst<P, R>(map.div, map.conq, childVisitor.stack));
    }

    public <P extends Serializable, R extends Serializable> void visit(
        Fork<P, R> fork) {
        Vector<Stack<Instruction>> v = new Vector<Stack<Instruction>>();

        for (Skeleton s : fork.subSkelList) {
            InstructionBuilderVisitor cv = new InstructionBuilderVisitor();
            s.accept(cv);
            v.add(cv.stack);
        }

        stack.add(new ForkInst<P, R>(fork.div, fork.conq, v));
    }

    public <P extends Serializable, R extends Serializable> void visit(
        DaC<P, R> dac) {
        InstructionBuilderVisitor childVisitor = new InstructionBuilderVisitor();
        dac.child.accept(childVisitor);

        stack.add(new DaCInst<P, R>(dac.div, dac.conq, dac.cond,
                childVisitor.stack));
    }
}
