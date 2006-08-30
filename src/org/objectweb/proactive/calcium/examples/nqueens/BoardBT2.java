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
package org.objectweb.proactive.calcium.examples.nqueens;

import java.util.Vector;

public class BoardBT2 extends BoardBT1 {
	
	int bound2, sidemask, lastmask, endbit;
	
	/**
	 * Constructor descriptor de tarea tipo backtrack2.
	 * 
	 * @author mleyton
	 * @param n
	 *            Tama~no del tabler nXn.
	 * @param row
	 *            Profundidad del arbol de busqueda.
	 * @param left
	 *            Diagonales ocupadas que crecen a la izquierda.
	 * @param down
	 *            Columnas ocupadas.
	 * @param right
	 *            Diagonales ocupadas que crecen a la derecha.
	 * @param bound1
	 *            Parametro necesario para backtrack2.
	 * @param bound2
	 *            Parametro necesario para backtrack2.
	 * @param sidemask
	 *            Parametro necesario para backtrack2.
	 * @param lastmask
	 *            Parametro necesario para backtrack2.
	 * @param topbit
	 *            Parametro necesario para backtrack2.
	 * @param mask
	 *            Parametro necesario para backtrack2.
	 * @param endbit
	 *            Parametro necesario para backtrack2.
	 * @param board
	 *            Arreglo con el tablero generado hasta la posicion: fila-1.
	 */
	public BoardBT2(int n, int solvableSize, int row, int left, int down, int right,
			int bound1, int bound2, int sidemask, int lastmask, int topbit,
			int mask, int endbit, int board[]) {
		
		super(n,solvableSize, row, left, down, right, bound1, board);
		
		this.bound2 = bound2;
		this.sidemask = sidemask;
		this.lastmask = lastmask;
		this.topbit = topbit;
		this.mask = mask;
		this.endbit = endbit;

		if (row == 1)
			this.board[0] = 1 << bound1;
		else if(board!=null){
			for (int i = 0; i < this.row; i++)
				this.board[i] = board[i];
		}
	}
	
	@Override
	public boolean isRootBoard(){
		return false;
	}
	
	@Override
	public boolean isBT1(){
		return false;
	}
	
	@Override
	public Vector<Board> divide() {

		Vector<Board> v = new Vector<Board>();

		int mask = (1 << n) - 1;
		int bitmap = mask & ~(left | down | right);
		int bit;

		if (row < bound1) {
			bitmap |= sidemask;
			bitmap ^= sidemask;
		} else if (row == bound2) {
			if ((down & sidemask) == 0) {
				// "return;" original alogirithm is converted into
				v.add(new Board(n, solvableSize)); //dummy child task
				return v; // no more search is required in this branch
			}
			if ((down & sidemask) != sidemask)
				bitmap &= sidemask;
		}
		while (bitmap != 0) {
			bitmap ^= board[row] = bit = -bitmap & bitmap;
			v.add(new BoardBT2(n, solvableSize, row + 1, (left | bit) << 1,
							down | bit, (right | bit) >> 1, bound1,
							bound2, sidemask, lastmask, topbit, mask,
							endbit, board));
		} // while-generando

		return v;
	}
	
	
}