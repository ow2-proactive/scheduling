/*
 * Created on May 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.examples.plugtest;

/**
 * @author mozonne
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;

public class ObjA implements InitActive,  EndActive{

        protected String s;
        protected ObjB b;
        protected String node;
        private int i;


        public ObjA() {

        }

        public ObjA (String s, ObjB b) {
                this.s  = s;
                this.i = new Integer (s.substring("object".length(), s.length())).intValue();
                this.b = b;
        }


        public String getInfo () {
                String property = System.getProperty("proactive.property");
                if(property != null) return s+" "+property;
                else{
                        return s;
                }

        }

        public int getNumber () {
                return i;
        }

        public ObjB getB () {
                return b;
        }

        public String getNode() {
        	return node;
        }

        public String toString() {
                return s;
        }
         public String sayHello(){
                return b.sayHello();
         }
//       -- implements InitActive
    public void initActivity(Body body) {
    	node = body.getNodeURL();
        System.out.println("I'm starting my activity");
        System.out.println(" I am on node "+ node);
       
    }

    public void endActivity(Body body) {
        System.out.println("I have finished my activity");
    }

}

