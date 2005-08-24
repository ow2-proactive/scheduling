package org.objectweb.proactive.examples.c3d;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * Storage for triples of (int -> User  -> String).
 * This is a Bag, because keys can be the same [even though they shouldn't be!].
 */
public class UserBag {
    private ArrayList list = new ArrayList();

    /** Create an empty Bag. This was not put just to obey the ProActive requirement 
     * (empty no-arg Constructor), because this object is not sent across networks */
    public UserBag() {
    }

    /**
     * Does not erase previous equal key, only makes another pair with this key.
     */
    public void add(int key, C3DUser value, String name) {
        list.add(new Troika(key, value, name));
    }

    /**
     * The number of triples in this bag.
     */
    public int size() {
        return list.size();
    }

    /**
     * The C3DUser which is attached to the FIRST occurence of this key in the Bag.
     * @returns null if no element corresponds
     */
    public C3DUser getUser(int key) {
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Troika pair = (Troika) iter.next();
            if (pair.key == key) {
                return pair.user;
            }
        }
        return null;
    }

    /**
     * Returns the name of the FIRST occurence of this key in the Bag.
     * @returns "" if no element corresponds, because I don't like null Strings.
     */
    public String getName(int key) {
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Troika pair = (Troika) iter.next();
            if (pair.key == key) {
                return pair.name;
            }
        }
        return "";
    }

    /**
     * Removes FIRST occurence of this key in the Bag, with its value
     * This does not allow to retrieve the name, which is lost.
     * To get the name, first use a getName().
     * @return the associated C3DUser, if there was one (null otherwise).
     */
    public C3DUser remove(int key) {
        int max = size();
        for (int i = 0; i < max; i++) {
            Troika pair = (Troika) list.get(i);
            if (pair.key == key) {
                list.remove(i);
                return pair.user;
            }
        }
        return null;
    }

    // iteration related variables
    private Iterator iterator;
    private Troika currentTroika;

    // Iteration primitives, just like any Java Collection
    public void newIterator() {
        iterator = list.iterator();
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public void next() {
        currentTroika = (Troika) iterator.next();
    }

    // On the current triple pointed; get the different values. 
    public int currentKey() {
        return currentTroika.key;
    }

    public C3DUser currentUser() {
        return currentTroika.user;
    }

    public String currentName() {
        return currentTroika.name;
    }

    // EXAMPLE USE :
    //    for (userBag.newIterator(); userBag.hasNext(); ) {
    //        userBag.next();
    //        ( userBag.currentUser()).showMessage(s_message);
    //    }
    
    // A container for (int -> C3DUser -> String)
    private class Troika {
        int key;
        C3DUser user;
        String name;

        public Troika(int key, C3DUser value, String name) {
            this.key = key;
            this.user = value;
            this.name = name;
        }
    }
}
