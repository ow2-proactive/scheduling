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
package trywithcatch;

import java.io.IOException;
import java.util.List;


public class Block extends Anything {
    private Terminal start;
    private Terminal end;
    private List<Anything> things;

    public Block(Terminal start, Terminal end, List<Anything> things) {
        this.start = start;
        this.end = end;
        this.things = things;
    }

    @Override
    public String toString() {
        return "{@" + start.getLeft() + "-" + end.getRight() + "}";
    }

    @Override
    public boolean isEmpty() {
        for (Anything a : things) {
            if (!a.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void prettyPrint(int indent) {
        start.prettyPrint(indent);
        indent++;
        for (Anything a : things) {
            a.prettyPrint(indent);
        }
        indent--;
        end.prettyPrint(indent);
    }

    @Override
    public void work(Catcher c) throws IOException {
        for (Anything a : things) {
            a.work(c);
        }
    }

    public Terminal getStart() {
        return start;
    }

    public Terminal getEnd() {
        return end;
    }
}
