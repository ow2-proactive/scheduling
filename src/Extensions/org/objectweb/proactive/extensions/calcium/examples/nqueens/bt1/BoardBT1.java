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
package org.objectweb.proactive.extensions.calcium.examples.nqueens.bt1;

import org.objectweb.proactive.extensions.calcium.examples.nqueens.Board;


public class BoardBT1 extends Board {

    /**
     * Constructor for backtrac1 boards
     *
     * @param n Size of the board
     * @param fila Profundidad del arbol de busqueda.
     * @param left Diagonales ocupadas que crecen a la izquierda.
     * @param down Columnas ocupadas.
     * @param right Diagonales ocupadas que crecen a la derecha.
     * @param bound1 Columna de la fila 1 para la cual se esta calculando.
     * @param board Arreglo con el tablero generado hasta la posicion fila-1.
     */
    public BoardBT1(int n, int solvableSize, int row, int left, int down,
        int right, int bound1, int[] board) {
        super(n, solvableSize, row, left, down, right, bound1);

        if (row == 2) {
            this.board[0] = 1;
            this.board[1] = 1 << bound1;
        } else if (board != null) {
            for (int i = 0; i < this.row; i++)
                this.board[i] = board[i];
        }

        //sidemask = (1 << (n - 1)) | 1;
        //lastmask = sidemask;
        topbit = 1 << (n - 1);
        mask = (1 << n) - 1;
        //endbit = topbit >> 1;
    }

    @Override
    public boolean isBT1() {
        return true;
    }

    @Override
    public boolean isRootBoard() {
        return false;
    }
}
