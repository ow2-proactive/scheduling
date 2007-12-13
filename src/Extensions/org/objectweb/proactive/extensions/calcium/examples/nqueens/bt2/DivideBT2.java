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

import java.util.Vector;

import org.objectweb.proactive.extensions.calcium.examples.nqueens.Board;
import org.objectweb.proactive.extensions.calcium.muscle.Divide;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystem;


public class DivideBT2 implements Divide<Board, Board> {
    public Vector<Board> divide(SkeletonSystem system, Board board) throws RuntimeException {
        if (board.isRootBoard()) {
            return initDivideBT2(board);
        }

        return divideBT2((BoardBT2) board);
    }

    private Vector<Board> initDivideBT2(Board board) {
        Vector<Board> v = new Vector<Board>();

        int sidemask = (1 << (board.n - 1)) | 1;
        int lastmask = sidemask;
        int topbit = 1 << (board.n - 1);
        int mask = (1 << board.n) - 1;
        int endbit = topbit >> 1;

        for (int i = 1, j = board.n - 2; i < j; i++, j--) {
            //bound1 = i; //bound2 = j;
            int bit = 1 << i;

            v.add(new BoardBT2(board.n, board.solvableSize, 1, bit << 1, bit, bit >> 1, i, j, sidemask,
                lastmask, topbit, mask, endbit, null));

            lastmask |= ((lastmask >> 1) | (lastmask << 1));
            endbit >>= 1;
        }

        return v;
    }

    public Vector<Board> divideBT2(BoardBT2 board) {
        Vector<Board> v = new Vector<Board>();

        int mask = (1 << board.n) - 1;
        int bitmap = mask & ~(board.left | board.down | board.right);
        int bit;

        if (board.row < board.bound1) {
            bitmap |= board.sidemask;
            bitmap ^= board.sidemask;
        } else if (board.row == board.bound2) {
            if ((board.down & board.sidemask) == 0) {
                // "return;" original algorithm is converted into
                v.add(new Board(board.n, board.solvableSize)); //dummy child task
                return v; // no more search is required in this branch
            }
            if ((board.down & board.sidemask) != board.sidemask) {
                bitmap &= board.sidemask;
            }
        }
        while (bitmap != 0) {
            bitmap ^= (board.board[board.row] = bit = -bitmap & bitmap);
            v.add(new BoardBT2(board.n, board.solvableSize, board.row + 1, (board.left | bit) << 1,
                board.down | bit, (board.right | bit) >> 1, board.bound1, board.bound2, board.sidemask,
                board.lastmask, board.topbit, board.mask, board.endbit, board.board));
        } // while-generating

        return v;
    }
}
