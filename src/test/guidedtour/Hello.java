package test.guidedtour;



import java.net.InetAddress;
import java.net.UnknownHostException;
/** This class represents an object, with its functionnal properties and
* behaviors.

*/
public class Hello {
        private String name;
        // ProActive requires a constructor with no parameter
        // in the class of the active object. That implies the presence
        // of a constructor with no parameter in the mother class
   /** constructor with no parameter, required by the ProActive model
   */ 
   public Hello() {
   }
         /** constructor
   * @param name the name of the agent
   *
   */ 
   public Hello(String name) {
   this.name = name;
   }
         /** a functionnal behavior of the object
   * @return what the agent has to say
   */
   public void sayHello() {
   //System.out.println("\nHELLO!");
	System.out.println("\n---------------------\nhello,from : " + getLocalHostName());
   }
   
   public void toto() {
   }
         /** locator method
   *
   * @return the name of the host currently containing the object
   */ 
   protected String getLocalHostName() {
         try {
                 return InetAddress.getLocalHost().toString();
           } catch (UnknownHostException uhe) {
                uhe.printStackTrace();
                  return "! localhost resolution failed";
      }
   }
}

