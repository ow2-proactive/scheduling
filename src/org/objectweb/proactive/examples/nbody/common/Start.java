package org.objectweb.proactive.examples.nbody.common;

import java.io.IOException;

/*
 * Created on Jan 7, 2005
 */

/**
 * @author irosenbe
 **/


public class Start {
    
      
     /**
      * Options should be "java Start xmlFile [-display] totalNbBodies maxIter"
      * @param -display, which is not compulsory, specifies whether a graphic display is to be created.
      * @param xmlFile is the xml deployment file..
      * @param totalNbBodies  The number of Planets in the System
      * @param maxIter The number of iterations before the program stops. 
      */
     public static void main(String[] args) {
         
         int input = 0; 
         
         System.out.print("Choose which version you want to run [123] : ");
         try {
             while ( true ) {
               // Read a character from keyboard
               input  = System.in.read();
               // 1 byte character is returned in int.
               // So cast to char
               if (input == 49 ||input == 50 ||input == 51 ||input == -1)
                   break; 
               }
           } catch (IOException ioe) {
               System.out.println( "IO error:" + ioe );
           } 
           System.out.println ("Thank you!");
           switch (input) {
            case 49 :  org.objectweb.proactive.examples.nbody.groupcom.Start.main(args); break;
          	case 50 :  org.objectweb.proactive.examples.nbody.barneshut.Start.main(args); break;
           	case 51 :  org.objectweb.proactive.examples.nbody.oospmd.Start.main(args); break;
           }

     }
}
