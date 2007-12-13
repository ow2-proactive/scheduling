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
import org.objectweb.proactive.extensions.calcium.examples.nqueens.Result;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.SolveBoard;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystem;


public class SolveBT2 extends SolveBoard {
    public Result execute(SkeletonSystem system, Board board) {
        n1 = board.n - 1;
        n2 = n1 - 1;
        BoardBT2 boardBT2 = (BoardBT2) board;
        Result res = new Result(board.n);
        backtrack2(res, (BoardBT2) board, boardBT2.row, boardBT2.left, boardBT2.down, boardBT2.right);
        return mixBoard(res, n1, n2);
    }

    private void check(Result res, BoardBT2 board) {
        int own;
        int you;
        int bit;
        int ptn;
        int firstColumn;
        int lastColumn;

        /* 90-degree rotation */
        if (board.board[board.bound2] == 1) {
            for (ptn = 2, own = 1; own <= n1; own++, ptn <<= 1) {
                bit = 1;
                for (you = n1; (board.board[you] != ptn) && (board.board[own] >= bit); you--)
                    bit <<= 1;
                if (board.board[own] > bit) {
                    return;
                }
                if (board.board[own] < bit) {
                    break;
                }
            }
            if (own > n1) {
                //count2();
                res.solutions[position(board.board[0])]++;
                //display();
                return;
            }
        }

        /* 180-degree rotation */
        if (board.board[n1] == board.endbit) {
            for (you = n2, own = 1; own <= n1; own++, you--) {
                bit = 1;
                for (ptn = board.topbit; (ptn != board.board[you]) && (board.board[own] >= bit); ptn >>= 1)
                    bit <<= 1;
                if (board.board[own] > bit) {
                    return;
                }
                if (board.board[own] < bit) {
                    break;
                }
            }
            if (own > n1) {
                //count4();
                res.solutions[position(board.board[0])]++;
                for (firstColumn = 1; (board.board[firstColumn] & 1) == 0; firstColumn++)
                    ;
                res.solutions[firstColumn]++;

                //display();
                return;
            }
        }

        /* 270-degree rotation */
        if (board.board[board.bound1] == board.topbit) {
            for (ptn = board.topbit >> 1, own = 1; own <= n1; own++, ptn >>= 1) {
                bit = 1;
                for (you = 0; (board.board[you] != ptn) && (board.board[own] >= bit); you++)
                    bit <<= 1;
                if (board.board[own] > bit) {
                    return;
                }
                if (board.board[own] < bit) {
                    break;
                }
            }
        }

        //count8();
        res.solutions[position(board.board[0])]++;
        res.solutions[position(board.board[n1])]++;
        for (firstColumn = 1; (board.board[firstColumn] & 1) == 0; firstColumn++)
            ;
        for (lastColumn = 1; (board.board[lastColumn] & board.topbit) == 0; lastColumn++)
            ;
        res.solutions[firstColumn]++;
        res.solutions[lastColumn]++;
        //display();
    }

    private void backtrack2(Result res, BoardBT2 board, int y, int left, int down, int right) {
        int bitmap = board.mask & ~(left | down | right);
        int bit;

        if (y == n1) {
            if (bitmap != 0) {
                if ((bitmap & board.lastmask) == 0) {
                    board.board[y] = bitmap;
                    check(res, board);
                }
            }
        } else {
            if (y < board.bound1) {
                bitmap |= board.sidemask;
                bitmap ^= board.sidemask;
            } else if (y == board.bound2) {
                if ((down & board.sidemask) == 0) {
                    return;
                }
                if ((down & board.sidemask) != board.sidemask) {
                    bitmap &= board.sidemask;
                }
            }
            while (bitmap != 0) {
                bitmap ^= (board.board[y] = bit = -bitmap & bitmap);
                backtrack2(res, board, y + 1, (left | bit) << 1, down | bit, (right | bit) >> 1);
            }
        }
    }
}
