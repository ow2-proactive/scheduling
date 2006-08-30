package org.objectweb.proactive.calcium.examples.nqueens;

import org.objectweb.proactive.calcium.interfaces.Execute;


public class SolveBoard implements Execute<Board> {
	
    private int n1;
    private int n2;

    //Worker main method
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

        int i;
        // mix the solutions to find the final ones
        if ((board.n % 2) == 0) {
            // n is even
            for (i = 0; i <= ((n1) / 2); i++) {
                board.solutions[i] += board.solutions[n1 - i];
            }
            for (i = n1; i >= (board.n / 2); i--) {
                board.solutions[i] = board.solutions[n1 - i];
            }
        } else {
            // n is odd
            for (i = 0; i <= (board.n / 2); i++) {
                board.solutions[i] += board.solutions[n1 - i];
            }
            for (i = n1; i > (board.n / 2); i--) {
                board.solutions[i] = board.solutions[n1 - i];
            }
        }
        return board;
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
                board.solutions[posicion(board.board[0])]++;
                board.solutions[posicion(board.board[n1])]++;
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

    private void check(BoardBT2 board) {
    	int own,you,bit,ptn, firstColumn, lastColumn;
    	
        /* 90-degree rotation */
        if (board.board[board.bound2] == 1) {
            for (ptn = 2, own = 1; own <= n1; own++, ptn <<= 1) {
                bit = 1;
                for (you = n1;
                        (board.board[you] != ptn) && (board.board[own] >= bit);
                        you--)
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
                board.solutions[posicion(board.board[0])]++;
                //display();
                return;
            }
        }

        /* 180-degree rotation */
        if (board.board[n1] == board.endbit) {
            for (you = n2, own = 1; own <= n1; own++, you--) {
                bit = 1;
                for (ptn = board.topbit;
                        (ptn != board.board[you]) && (board.board[own] >= bit);
                        ptn >>= 1)
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
                board.solutions[posicion(board.board[0])]++;
                for (firstColumn = 1; (board.board[firstColumn] & 1) == 0;
                        firstColumn++)
                    ;
                board.solutions[firstColumn]++;

                //display();
                return;
            }
        }

        /* 270-degree rotation */
        if (board.board[board.bound1] == board.topbit) {
            for (ptn = board.topbit >> 1, own = 1; own <= n1;
                    own++, ptn >>= 1) {
                bit = 1;
                for (you = 0;
                        (board.board[you] != ptn) && (board.board[own] >= bit);
                        you++)
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
        board.solutions[posicion(board.board[0])]++;
        board.solutions[posicion(board.board[n1])]++;
        for (firstColumn = 1; (board.board[firstColumn] & 1) == 0;
                firstColumn++)
            ;
        for (lastColumn = 1; (board.board[lastColumn] & board.topbit) == 0;
                lastColumn++)
            ;
        board.solutions[firstColumn]++;
        board.solutions[lastColumn]++;
        //display();
    }

    private void backtrack2(BoardBT2 board, int y, int left, int down, int right) {
        int bitmap = board.mask & ~(left | down | right);
        int bit;
        
        if (y == n1) {
            if (bitmap != 0) {
                if ((bitmap & board.lastmask) == 0) {
                    board.board[y] = bitmap;
                    check(board);
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
                backtrack2(board, y + 1, (left | bit) << 1, down | bit,
                    (right | bit) >> 1);
            }
        }
    }


    private int posicion(int bit) {
    	int i;
        for (i = 0; (bit >>= 1) != 0; i++)
            ;
        return i;
    }

    // prints the board
    private void display(BoardBT1 board) {

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

    private void printbit(Board board, int valor) {
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
