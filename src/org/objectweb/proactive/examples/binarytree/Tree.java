/* 
* ################################################################
* 
* ProActive: The Java(TM) library for Parallel, Distributed, 
*            Concurrent computing with Security and Mobility
* 
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
* 
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*  
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
* USA
*  
*  Initial developer(s):               The ProActive Team
*                        http://www.inria.fr/oasis/ProActive/contacts.html
*  Contributor(s): 
* 
* ################################################################
*/ 
package org.objectweb.proactive.examples.binarytree;

public class Tree {

  private String key;
  private String value;
  private Tree left;
  private Tree right;
  private TreeDisplay display;


  public Tree() {
  }


  public Tree(String key, String value, TreeDisplay display) {
    this.left = null;
    this.right = null;
    this.key = key;
    this.value = value;
    this.display = display;
    display.displayMessage("[" + key + "] Createdwith value " + value);
  }


  public void insert(String key, String value) {
    int res = key.compareTo(this.key);
    if (res == 0) {
      // Same key --> Modify the current calue
      display.displayMessage("[" + key + "] Replacing " + this.value + " with " + value);
      this.value = value;
    } else if (res < 0) {
      display.displayMessage("[" + key + "] trying left");
      // key < this.key --> store left
      if (left != null) {
        left.insert(key, value);
      } else {
        display.displayMessage("[" + key + "] CREATE left");
        // Create the new node
        try {
          left = (Tree)org.objectweb.proactive.ProActive.newActive(this.getClass().getName(), new Object[]{key,value,display});
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } else {
      display.displayMessage("[" + key + "] trying right");
      if (right != null) {
        right.insert(key, value);
      } else {
        display.displayMessage("[" + key + "] Creating right");
        try {
          right = (Tree)org.objectweb.proactive.ProActive.newActive(this.getClass().getName(), new Object[]{key,value,display});
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }


  public String search(String key) {
    display.displayMessage("[" + this.key + "] Searching for " + key);
    if (key == null)
      return null;

    int res = key.compareTo(this.key);
    if (res == 0) {
      display.displayMessage("[" + this.key + "] Found " + key);
      return value;
    }
    if (res < 0)
      return (left != null)?left.search(key):null;
    else
      return (right != null)?right.search(key):null;
  }


  public String getKey() {
    return key;
  }


  public String getValue() {
    return value;
  }


  public Tree getLeft() {
    return left;
  }


  public Tree getRight() {
    return right;
  }


  public StringBuffer dump(int level, boolean draw) {
    StringBuffer str = new StringBuffer();
    str.append("[").append(key).append("] --> ").append(value).append("\n");
    
    String spaces = "";
    if (right != null || left != null) {
      StringBuffer indent = new StringBuffer();
      for (int i = 0; i < level; i++) 
        indent.append("  ");
      if (draw) indent.setCharAt(indent.length() - 1, '|');
      spaces = indent.toString();
    }
    
    if (left != null) {
      str.append(spaces).append(" |\n");
      str.append(spaces).append(" +->");
      str.append(left.dump(level + 1, (right != null)).toString());
    }
    if (right != null) {
      str.append(spaces).append(" |\n");
      str.append(spaces).append(" +->");
      str.append(right.dump(level + 1, false).toString());
    }
    return str;
  }
}
