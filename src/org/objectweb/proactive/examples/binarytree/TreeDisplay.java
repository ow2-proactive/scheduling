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

public class TreeDisplay {

  private TreeApplet applet;
  private Tree tree;


  public TreeDisplay() {
  }


    public TreeDisplay(TreeApplet applet) {
      this.applet = applet;
      tree = null;
    }
    
    
    public void displayMessage(String s) {
	applet.receiveMessage(s);
    }
    
    public void displayMessage(String s, java.awt.Color c) {
      applet.receiveMessage(s, c);
    }

    
    // Create a tree with random nodes
    public void createTree(int size, boolean AC) {
	// Reset the current tree to create another
	deleteTree();
	String[] keys = randomKeys(size);
	addRandom(keys, AC);
    }

    
    public void add(String key, String value, boolean AC) {
	if (tree == null) {
	    try {
		tree = (Tree)org.objectweb.proactive.ProActive.newActive(Tree.class.getName(), new Object[]{key, value, org.objectweb.proactive.ProActive.getStubOnThis()});
		applet.receiveMessage("Creating initial tree",
				      new java.awt.Color(0, 150, 0));
	    } 
	    catch (Exception e) {
		applet.receiveMessage("Waiting, program is lauching...",
				      java.awt.Color.red);
		e.printStackTrace();
	    }
	}
	else {
	    tree.insert(key, value, AC);
	}
	applet.displayTree();
    }


    public void deleteTree() {
	if (tree != null) {
	    tree.delete();
	    tree = null;
	    applet.receiveMessage("Current tree erased!", java.awt.Color.red);
	}
    }

    
    public Tree getTree() {
	return tree;
    }
    
    
    public ObjectWrapper search(String key) {
	if (tree == null)
	    return null;
	return tree.search(key);
    }


    // Return a specified number of random keys
    public java.util.ArrayList getRandomKeys(int number) {
	java.util.ArrayList result = new java.util.ArrayList();
	java.util.ArrayList keys = getKeys();
	if (keys.size() < number)
	    number = keys.size();
	while (number > 0) {
	    java.util.Random random = new java.util.Random();
	    int index = random.nextInt(keys.size());
	    result.add(keys.get(index));
	    keys.remove(index);
	    number--;
	}
	return result;
    }


    // Return the key list of the tree. 
    public java.util.ArrayList getKeys() {
	java.util.ArrayList keys = new java.util.ArrayList();
	if (tree != null) {
	    keys = tree.getKeys();
	}
	return keys;
    }


    // Change Automatic Continuation state
    public void enableAC() {
	if (tree == null)
	    return;
	tree.enableAC();
    }


    public void disableAC() {
	if (tree == null)
	    return;
	tree.disableAC();
    }

    /********************************************
     *    Methods to create a random tree       *
     ********************************************/

  
    private String[] randomKeys(int number) {
	java.util.TreeSet keys = new java.util.TreeSet();
	for (int i=0; i<number; i++) {
	    while (!keys.add(randomWord(4))) {
	    }
	}
	int i = 0;
	String[] result = new String[number];
	java.util.Iterator it = keys.iterator();
	while (it.hasNext()) {
	    result[i++] = (String)it.next();
	}
	return result;
    }
    

    private void addRandom(String[] t, boolean AC) {
	if (t.length == 0)
	    return;
	else if (t.length == 1)
	    add(t[0], randomWord(4), AC);
	else if (t.length == 2) {
	    add(t[0], randomWord(4), AC);
	    add(t[1], randomWord(4), AC);
	}
	else {
	    add(t[(int)t.length/2], randomWord(4), AC);
	    addRandom(firstHalf(t), AC);
	    addRandom(secondHalf(t), AC);
	}
    }


    private String randomWord(int size) {
	java.util.Random random = new java.util.Random();
	String result = "";
	for (int i=0; i < size; i++) {
	    int code = random.nextInt(35);
	    char[] c = {Character.forDigit(code,35)};
	    result = result.concat(String.copyValueOf(c));
	}
	return result;
    }


    private String[] firstHalf(String[] t) {
	String[] firstHalf = new String[(int)(t.length/2)];
	for (int i=0; i<(t.length/2); i++) {
	    firstHalf[i] = t[i];
	}
	return firstHalf;
    }
    
    private String[] secondHalf(String[] t) {
	int size = t.length - ((int)(t.length/2) + 1);
	String[] secondHalf = new String[size];
	int j = 0;
	for (int i=((int)(t.length/2) + 1); i<t.length; i++) {
	    secondHalf[j++] = t[i];
	}
	return secondHalf;
    }
}
