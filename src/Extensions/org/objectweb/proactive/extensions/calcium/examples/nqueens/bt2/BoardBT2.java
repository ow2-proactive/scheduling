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
package org.objectweb.proactive.extensions.calcium.examples.nqueens.bt2;

import org.objectweb.proactive.extensions.calcium.examples.nqueens.Board;


public class BoardBT2 extends Board {
    int bound2;
    int sidemask;
    int lastmask;
    int endbit;

    public BoardBT2(int n, int solvableSize, int row, int left, int down,
        int right, int bound1, int bound2, int sidemask, int lastmask,
        int topbit, int mask, int endbit, int[] board) {
        super(n, solvableSize, row, left, down, right, bound1);

        this.topbit = topbit;
        this.mask = mask;
        this.bound2 = bound2;
        this.sidemask = sidemask;
        this.lastmask = lastmask;
        this.endbit = endbit;

        if (row == 1) {
            this.board[0] = 1 << bound1;
        } else if (board != null) {
            for (int i = 0; i < this.row; i++)
                this.board[i] = board[i];
        }
    }

    @Override
    public boolean isRootBoard() {
        return false;
    }

    @Override
    public boolean isBT1() {
        return false;
    }
}
