/*
 * Created on Jan 14, 2005
 */
package org.objectweb.proactive.examples.nbody.barneshut;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.examples.nbody.common.Point2D;
import org.objectweb.proactive.examples.nbody.common.Rectangle;

/**
 * @author irosenbe
 * @see also http://www.reed.edu/~jimfix/MSRI/quadtree/Quadtree.java
 */
public class QuadTree implements Serializable {
    
    private final int MAX_BODIES_IN_DOMAIN = 3;
    
    public int label;
    Info info;
    QuadTree Q[];
    
    public QuadTree() {}
    
    /**
     * Construct a QuadTree with structure all set up, including labels, having nbPlanets Planets inside.
     * @param nbPlanets The number of Planets to use for the construction of this QuadTree 
     */
    public QuadTree(int nbPlanets) {
        Rectangle R = new Rectangle (-100,-100,200,200);
        Vector v = new Vector ();	// of type vector of planets
        for (int i = 0 ; i < nbPlanets ; i++ )
            v.add (new Planet(R,i));
        myConstructor(R, v);
        label();
    }
    
    
    /**
     * Construct a QuadTree within the bounds given by R, with Bodies specified in the Vector
     * @param bodies (type Vector of Planet) all the bodies in the QuadTree
     * @param R the bounding rectangle, which contains all the bodies
     */
    public QuadTree(Rectangle R, Vector bodies) {
        myConstructor(R,bodies);
    }
    
    /**
     * @param bodies (type Vector of Planet) all the bodies in the QuadTree
     * @param R the bounding rectangle, which contains all the bodies
     * 
     */
    private void myConstructor(Rectangle R, Vector bodies) {
        info = new Info(bodies,R);
        
        if (info.numPlanets >  MAX_BODIES_IN_DOMAIN)	{ 	    // Split into sub-trees
            
            Point2D middle = average(bodies);
            
            Vector [] subtree = new Vector [4]; // list of bodies in each subtree, type is Vector of Info
            for (int i = 0 ; i < 4 ; i++)  
                subtree[i] = new Vector();
            for (int i = 0 ; i < info.numPlanets ; i++) {
                Info body = (Info) bodies.get(i);
                int index = (body.x < middle.x ? 0 : 1)  + ( body.y < middle.y ? 0 : 2) ;
                subtree[index].add(body);
            }
            
            
            // Only add to the root the trees which are not empty, ie which contain bodies
            Vector trees = new Vector();
            if (!subtree[0].isEmpty())
                trees.add( new QuadTree(new Rectangle(new Point2D(R.x,R.y),new Point2D(middle.x, middle.y)), subtree[0])) ; 
            if (!subtree[1].isEmpty())
                trees.add( new QuadTree(new Rectangle(new Point2D(middle.x,R.y),new Point2D(R.x + R.width, middle.y)), subtree[1])) ;
            if (!subtree[2].isEmpty())
                trees.add( new QuadTree(new Rectangle(new Point2D(R.x,middle.y),new Point2D(middle.x,R.y + R.height)), subtree[2])); 
            if (!subtree[3].isEmpty())
                trees.add( new QuadTree(new Rectangle(new Point2D(middle.x, middle.y), new Point2D(R.x + R.width, R.y + R.height)), subtree[3]));  
            
            //  Q=(QuadTree[]) trees.toArray(); 
            Q = new QuadTree[trees.size()];
            for (int i = 0 ; i < Q.length ; i++ )
                Q[i]=(QuadTree) trees.get(i);
        }
        else { // no sons to this Node 
            Q = null; 
        }
    }
    
    
    /**
     * Work out the average x and y coordinates of the given points
     * @param bodyList List of Bodies, of type Info Vector
     * @return the point which lies at average x and y coordinates
     */
    private Point2D average(Vector bodies) {
        Point2D average = new Point2D() ;
        for (int i = 0 ; i < info.numPlanets ; i++) {
            Info body = (Info) bodies.get(i);
            average.x += body.x;
            average.y += body.y;
        }
        average.x /= info.numPlanets;
        average.y /= info.numPlanets;
        return average;
    }
    
    
    //Adds a body to this quadtree
    //void insert(Info body) {throw new NullPointerException("Method not written");}
 
    
    //Removes a body from this quadtree
    //void remove(Info body)  {throw new NullPointerException("Method not written");}
 
    
    /*
     * Helper methods
     */
    
    /**
     * The size of the QuadTree.
     * @return the number of nodes in the tree
     */
    public int size () {
        if (Q != null) {
            int res = 1;
            for (int i = 0 ; i < Q.length ; i++)
                res += Q[i].size() ;
            return res; 
        }
        else 
            return 1;     
    }
    
    /**
     * Gives a unique label to every node on this quadTree
     * To work effectively, this node must be the root of the quadTree considered 
     */
    public void label(){
        label(0);
    }
    
    /*
     * Tech detail : works the following way  
     * 1) label first node with index.
     * 2) increment index. 
     * 3) Label next node (son or brother or uncle) with index
     * 4) loop 2) 
     */
    private int label(int index){
        this.label = index;
        index++;
        if (Q != null) {
            for (int i = 0 ; i < Q.length ; i++)
                index = Q[i].label(index) ;
        }
        return index;
    }
    
    /**
     * A stripped down visual view of the object.
     */
    public String toString() {
        return toString("");
    }
    
    private String toString(String add) {
        String res = "";
        if (Q != null) {
            for (int i = 0 ; i < Q.length ; i++)
                res = Q[i].toString(add + " ") + "\n" + res ;
            return add + "i=" + label + " M="+info.mass + " nb=" + info.numPlanets + " sons=" + "\n" + res;
        }
        else 
            return add + "i=" + label +" M="+info.mass + " nb=" + info.numPlanets;
    }
    
    /**
     * When you're looking for a node in the tree, and know it's label, get the node through this method.
     * @param n an integer expected to be a node label
     * @return The Node of the quadtree with label n
     * @throws java.util.NoSuchElementException if n >= size()
     */
    /*
     * Tech detail : Creates a list of nodes and checks if the top is the target 
     * If n = list.head.label, return.
     * else, pop top from list, add sons, and loop 
     */
    public QuadTree getNode(int n) {
        Vector nodes = new Vector();
        QuadTree t = this;
        try {
            while (true){
                if (t.label == n) {
                    return t;
                }
                if (t.Q != null) {
                    for (int i = 0 ; i < t.Q.length ; i++ )
                        nodes.add(t.Q[ i ]);
                }
                t = (QuadTree) nodes.remove(0);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new java.util.NoSuchElementException("No element with id = " + n + " in this tree.");
        }
    }
    
    
    /**
     * Sets the physics of this node, with id given as the second parameter
     * @param inf the new value of Info  
     * @param id the node number
     */
    public void setNodeInfo(Info inf) {
        QuadTree t = getNode (inf.identification);
        t.info = inf;
    }
    
    /**
     * Cleans up all the Info stored in this tree, replacing them by null
     */
    /*
     public void removeInfo() {
     Vector nodes = new Vector();
     QuadTree t = this;
     while ( !nodes.isEmpty() ){
     t = (QuadTree) nodes.remove(0);
     t.info = null;
     if (t.Q != null) {
     for (int i = 0 ; i < t.Q.length ; i++ )
     nodes.add(t.Q[ i ]);
     }
     }
     }*/
    
    /**
     * Given a Node, computes its coordinates and mass from underlying planets.
     * This may be called only once all its sons have been updated
     */
    public void recomputeCenterOfMass() {
        
        Info [] planets;
        
        if (Q==null) {  // If no other nodes, make the array the list of planets
            Vector dummy = info.getPlanets(); 
            planets = new Planet[dummy.size()];
            for (int i = 0 ; i < planets.length ; i++ )
                planets[i]=(Planet) dummy.get(i);
        }
        
        else { // if this node has sons, sum up the values of these subnodes
            planets = new Info[Q.length];
            for (int i = 0 ; i < Q.length ; i++ )
                planets[i] = Q[i].info;
        }
        
        // make the average, store it in info
        info.setCenterOfMass(planets);
    }
    
    
}
