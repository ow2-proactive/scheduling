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
package org.objectweb.proactive.extensions.calcium.examples.nqueens;

import org.objectweb.proactive.extensions.calcium.muscle.Execute;


abstract public class SolveBoard implements Execute<Board, Result> {
    protected int n1;
    protected int n2;

    public SolveBoard() {
    }

    protected int position(int bit) {
        int i;
        for (i = 0; (bit >>= 1) != 0; i++)
            ;
        return i;
    }

    //Worker main method
    /*
    public Board execute(Board board) {

        n1 = board.n - 1;
        n2 = n1 - 1;

        if (board.isBT1()) {
                BoardBT1 boardBT1=(BoardBT1)board;
            backtrack1(boardBT1, boardBT1.row, boardBT1.left, boardBT1.down, boardBT1.right);
        } else {
                BoardBT2 boardBT2=(BoardBT2)board;
            backtrack2((BoardBT2)board, boardBT2.row, boardBT2.left, boardBT2.down, boardBT2.right);
        }
    }
     */
    protected Result mixBoard(Result res, int n1, int n2) {
        int i;

        // mix the solutions to find the final ones
        if ((res.n % 2) == 0) {
            // n is even
            for (i = 0; i <= ((n1) / 2); i++) {
                res.solutions[i] += res.solutions[n1 - i];
            }
            for (i = n1; i >= (res.n / 2); i--) {
                res.solutions[i] = res.solutions[n1 - i];
            }
        } else {
            // n is odd
            for (i = 0; i <= (res.n / 2); i++) {
                res.solutions[i] += res.solutions[n1 - i];
            }
            for (i = n1; i > (res.n / 2); i--) {
                res.solutions[i] = res.solutions[n1 - i];
            }
        }
        return res;
    }

    // prints the board
    protected void display(Board board) {
        int y;
        int bit;
        System.out.println("N= " + board.n);

        for (y = 0; y < board.n; y++) {
            for (bit = board.topbit; bit != 0; bit >>= 1) {
                int aux = board.board[y] & bit;
                if (aux > 0) {
                    System.out.print("Q ");
                } else {
                    System.out.print("- ");
                }
            }
            System.out.println();
        }
    }

    protected void printbit(Board board, int valor) {
        int bit = 1 << (n1);
        int aux;
        for (int i = 0; i < board.n; i++) {
            aux = valor & bit;
            if (aux != 0) {
                System.out.print("1  ");
            } else {
                System.out.print("0  ");
            }
            valor = valor & (~aux);
            bit = bit >> 1;
        }
        System.out.println();
    }
}
