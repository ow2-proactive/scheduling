package org.objectweb.proactive.examples.nbody.barneshut;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.examples.nbody.common.Point2D;
import org.objectweb.proactive.examples.nbody.common.Rectangle;

/**
 * @see also <a href="http://www.reed.edu/~jimfix/MSRI/quadtree/Quadtree.java">this page</a>.
 */
public class QuadTree implements Serializable {
    
    private final int MAX_BODIES_IN_DOMAIN = 1;
    
    public int label;
    private Info info;
    QuadTree Q[];
    
    public QuadTree() {}
    
    /**
     * Construct a QuadTree with structure all set up, including labels, having nbPlanets Planets inside.
     * @param nbPlanets The number of Planets to use for the construction of this QuadTree 
     */
    public QuadTree(int nbPlanets) {
        Rectangle R = new Rectangle (-100,-100,100,100);
        Vector v = new Vector ();	// of type vector of planets
        for (int i = 0 ; i < nbPlanets ; i++ )
            v.add (new Planet(R, i));
        myConstructor(R, v);
        label(0);
    }
    
    
    /**
     * Construct a QuadTree with structure all set up, including labels, from the file given.
     * @param fileName, the file where to find the information on the Planets 
     */
    //	public QuadTree(String fileName) {
    //		Rectangle R = new Rectangle (-100,-100,200,200);
    //		Vector v = new Vector ();	// of type vector of planets
    //				Planet p = new Planet(x,y,vx,vy,mass);
    //				System.out.println("Planet : " + p);
    //				v.add (p);
    //			throw new NullPointerException("File " + fileName + " couldn't be closed.");
    //		myConstructor(R, v);
    //		label(0);
    //	}
    
    
    /**
     * Construct a QuadTree within the bounds given by R, with Bodies specified in the Vector
     * @param bodies (type Vector of Planet) all the bodies in the QuadTree
     * @param R the bounding rectangle, which contains all the bodies
     */
    private QuadTree(Rectangle R, Vector bodies) {
        myConstructor(R,bodies);
    }
    
    /**
     * @param bodies (type Vector of Planet) all the bodies in the QuadTree
     * @param R the bounding rectangle, which contains all the bodies
     * 
     */
    private void myConstructor(Rectangle R, Vector bodies) {
        this.info = new Info(bodies,R);
        
        if (bodies.size() >  MAX_BODIES_IN_DOMAIN)	{ 	    // Split into sub-trees
            
            Point2D middle = average(bodies);
            
            Vector [] subtree = new Vector [4]; // list of bodies in each subtree, type is Vector of Info
            for (int i = 0 ; i < 4 ; i++)  
                subtree[i] = new Vector();
            for (int i = 0 ; i < bodies.size() ; i++) {
                Planet body = (Planet) bodies.get(i);
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
            
            this.Q=(QuadTree[]) trees.toArray(new QuadTree[] {}); 
        }
        else { // no sons to this Node 
            this.Q = null; 
        }
    }
    
    
    /**
     * Work out the average x and y coordinates of the given points
     * @param bodyList List of Bodies, of type Info Vector
     * @return the point which lies at average x and y coordinates
     */
    private Point2D average(Vector bodies) {
        Point2D average = new Point2D() ;
        for (int i = 0 ; i < bodies.size() ; i++) {
            Info body = (Info) bodies.get(i);
            average.x += body.x;
            average.y += body.y;
        }
        average.x /= bodies.size();
        average.y /= bodies.size();
        return average;
    }
    
    
    /*
     * Helper methods
     */
    
    
    /*
     * Gives a unique label to every node on this quadTree
     * To work effectively, start by root.label(0)
     * Tech detail : works the following way  
     * 1) label first node with index.
     * 2) increment index. 
     * 3) Label next node (son or brother or uncle) with index
     * 4) loop 2) 
     */
    private int label(int index){
        this.label = index;
        this.info.identification = label;
        index++;
        if (this.Q != null) {
            for (int i = 0 ; i < this.Q.length ; i++)
                index = this.Q[i].label(index) ;
        }
        return index;
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
     * Returns a Vector of all the infos contained in the Tree, with index the node label.
     * It also "cleans" the tree, removing the Info from the Nodes.
     * To get the labels right, this node must be the root of the quadTree considered 
     */
    public Vector getInfo(){
        Vector v = new Vector ();
        info.clean(this.Q==null); // DEBUG : this causes trouble
        v.add(info);
        if (this.Q!=null) {
            for (int i = 0 ; i < this.Q.length ; i++) 
                v.addAll(this.Q[i].getInfo());
            info.setNbSons(this.Q.length);
        }
        info = null;	 // make the tree as light as possible
        return v;
    }
    
    /**
     * The size of the QuadTree.
     * @return the number of nodes in the tree
     */
    public int size () {
        if (this.Q != null) {
            int res = 1;
            for (int i = 0 ; i < this.Q.length ; i++)
                res += this.Q[i].size() ;
            return res; 
        }
        else 
            return 1;     
    }
    
    /**
     * A stripped down visual view of the object.
     */
    public String toString() {
        return toString("");
    }
    
    private String toString(String add) {
        String res = "";
        if (this.Q != null) {
            for (int i = 0 ; i < this.Q.length ; i++)
                res = this.Q[i].toString(add + " ") + "\n" + res ;
            //			return add + "i=" + label + " M="+info.mass + " nb=" + info.planets.length + " sons=" + "\n" + res;
            return add + "i=" + label + " sons=" + "\n" + res;
        }
        else 
            //			return add + "i=" + label +" M="+info.mass + " nb=" + info.planets.length;
            return add + "i=" + label ;
    }
    
}
