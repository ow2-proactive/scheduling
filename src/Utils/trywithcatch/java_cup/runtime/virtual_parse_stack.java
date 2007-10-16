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

import java.util.Stack;

/** This class implements a temporary or "virtual" parse stack that 
 *  replaces the top portion of the actual parse stack (the part that 
 *  has been changed by some set of operations) while maintaining its
 *  original contents.  This data structure is used when the parse needs 
 *  to "parse ahead" to determine if a given error recovery attempt will 
 *  allow the parse to continue far enough to consider it successful.  Once 
 *  success or failure of parse ahead is determined the system then 
 *  reverts to the original parse stack (which has not actually been 
 *  modified).  Since parse ahead does not execute actions, only parse
 *  state is maintained on the virtual stack, not full Symbol objects.
 *
 * @see     java_cup.runtime.lr_parser
 * @version last updated: 7/3/96
 * @author  Frank Flannery
 */

public class virtual_parse_stack {
  /*-----------------------------------------------------------*/
  /*--- Constructor(s) ----------------------------------------*/
  /*-----------------------------------------------------------*/

  /** Constructor to build a virtual stack out of a real stack. */
  public virtual_parse_stack(Stack shadowing_stack) throws java.lang.Exception
    {
      /* sanity check */
      if (shadowing_stack == null)
	throw new Exception(
	  "Internal parser error: attempt to create null virtual stack");

      /* set up our internals */
      real_stack = shadowing_stack;
      vstack     = new Stack();
      real_next  = 0;

      /* get one element onto the virtual portion of the stack */
      get_from_real();
    }

  /*-----------------------------------------------------------*/
  /*--- (Access to) Instance Variables ------------------------*/
  /*-----------------------------------------------------------*/
       
  /** The real stack that we shadow.  This is accessed when we move off
   *  the bottom of the virtual portion of the stack, but is always left
   *  unmodified.
   */
  protected Stack real_stack;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Top of stack indicator for where we leave off in the real stack.
   *  This is measured from top of stack, so 0 would indicate that no
   *  elements have been "moved" from the real to virtual stack. 
   */
  protected int real_next;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** The virtual top portion of the stack.  This stack contains Integer
   *  objects with state numbers.  This stack shadows the top portion
   *  of the real stack within the area that has been modified (via operations
   *  on the virtual stack).  When this portion of the stack becomes empty we 
   *  transfer elements from the underlying stack onto this stack. 
   */
  protected Stack vstack;

  /*-----------------------------------------------------------*/
  /*--- General Methods ---------------------------------------*/
  /*-----------------------------------------------------------*/

  /** Transfer an element from the real to the virtual stack.  This assumes 
   *  that the virtual stack is currently empty.  
   */
  protected void get_from_real()
    {
      Symbol stack_sym;

      /* don't transfer if the real stack is empty */
      if (real_next >= real_stack.size()) return;

      /* get a copy of the first Symbol we have not transfered */
      stack_sym = (Symbol)real_stack.elementAt(real_stack.size()-1-real_next);

      /* record the transfer */
      real_next++;

      /* put the state number from the Symbol onto the virtual stack */
      vstack.push(new Integer(stack_sym.parse_state));
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Indicate whether the stack is empty. */
  public boolean empty()
    {
      /* if vstack is empty then we were unable to transfer onto it and 
	 the whole thing is empty. */
      return vstack.empty();
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/
      
  /** Return value on the top of the stack (without popping it). */
  public int top() throws java.lang.Exception
    {
      if (vstack.empty())
	throw new Exception(
		  "Internal parser error: top() called on empty virtual stack");

      return ((Integer)vstack.peek()).intValue();
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Pop the stack. */
  public void pop() throws java.lang.Exception
    {
      if (vstack.empty())
	throw new Exception(
		  "Internal parser error: pop from empty virtual stack");

      /* pop it */
      vstack.pop();

      /* if we are now empty transfer an element (if there is one) */
      if (vstack.empty())
        get_from_real();
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Push a state number onto the stack. */
  public void push(int state_num)
    {
      vstack.push(new Integer(state_num));
    }

  /*-----------------------------------------------------------*/

}
