/*
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 * 
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 * 
 * ################################################################
 */
package org.objectweb.proactive.calcium.examples.nqueens.bt1;

import org.objectweb.proactive.calcium.examples.nqueens.Board;

public class SolveBT1 extends org.objectweb.proactive.calcium.examples.nqueens.SolveBoard{

	public SolveBT1() {
		super();
	}

	public Board execute(Board board){
       n1 = board.n - 1;
       n2 = n1 - 1;
       BoardBT1 boardBT1=(BoardBT1)board;
       backtrack1(boardBT1, boardBT1.row, boardBT1.left, boardBT1.down, boardBT1.right);
       return mixBoard(board, n1, n2);
	}
	
	/**
     * Metodo que calcula las tareas de tipo BT1
     * @param y
     * @param left
     * @param down
     * @param right
     * @return
     */
    private void backtrack1(BoardBT1 board, int y, int left, int down, int right) {
        int bitmap = board.mask & ~(left | down | right);
        int bit,firstColumn, lastColumn;
        
        if (y == n1) {
            if (bitmap != 0) {
                board.board[y] = bitmap;
                //count8();
                board.solutions[position(board.board[0])]++;
                board.solutions[position(board.board[n1])]++;
                for (firstColumn = 0; (board.board[firstColumn] & 1) == 0;
                        firstColumn++)
                    ;
                for (lastColumn = 1;
                        (board.board[lastColumn] & board.topbit) == 0;
                        lastColumn++)
                    ;
                board.solutions[firstColumn]++;
                board.solutions[lastColumn]++;
            }
        } else {
            if (y < board.bound1) {
                bitmap &= 0xFFFFFFFD; // 1111...01
            }
            while (bitmap != 0) {
                bitmap ^= (board.board[y] = bit = -bitmap & bitmap);
                backtrack1(board, y + 1, (left | bit) << 1, down | bit,
                    (right | bit) >> 1);
            }
        }
    }
}
