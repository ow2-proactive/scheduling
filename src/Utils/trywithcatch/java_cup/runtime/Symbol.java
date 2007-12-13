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
package trywithcatch.java_cup.runtime;

/**
 * Defines the Symbol class, which is used to represent all terminals
 * and nonterminals while parsing.  The lexer should pass CUP Symbols 
 * and CUP returns a Symbol.
 *
 * @version last updated: 7/3/96
 * @author  Frank Flannery
 */

/* ****************************************************************
 Class Symbol
 what the parser expects to receive from the lexer. 
 the token is identified as follows:
 sym:    the symbol type
 parse_state: the parse state.
 value:  is the lexical value of type Object
 left :  is the left position in the original input file
 right:  is the right position in the original input file
 ******************************************************************/

public class Symbol {

    /*******************************
     Constructor for l,r values
     *******************************/

    public Symbol(int id, int l, int r, Object o) {
        this(id);
        left = l;
        right = r;
        value = o;
    }

    /*******************************
     Constructor for no l,r values
     ********************************/

    public Symbol(int id, Object o) {
        this(id, -1, -1, o);
    }

    /*****************************
     Constructor for no value
     ***************************/

    public Symbol(int id, int l, int r) {
        this(id, l, r, null);
    }

    /***********************************
     Constructor for no value or l,r
     ***********************************/

    public Symbol(int sym_num) {
        this(sym_num, -1);
        left = -1;
        right = -1;
        value = null;
    }

    /***********************************
     Constructor to give a start state
     ***********************************/
    Symbol(int sym_num, int state) {
        sym = sym_num;
        parse_state = state;
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /** The symbol number of the terminal or non terminal being represented */
    public int sym;

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /** The parse state to be recorded on the parse stack with this symbol.
     *  This field is for the convenience of the parser and shouldn't be 
     *  modified except by the parser. 
     */
    public int parse_state;
    /** This allows us to catch some errors caused by scanners recycling
     *  symbols.  For the use of the parser only. [CSA, 23-Jul-1999] */
    boolean used_by_parser = false;

    /*******************************
     The data passed to parser
     *******************************/

    public int left, right;
    public Object value;

    /*****************************
      Printing this token out. (Override for pretty-print).
     ****************************/
    @Override
    public String toString() {
        return "#" + sym;
    }
}
