package active;
import org.objectweb.proactive.core.util.wrapper.StringWrapper; 
import java.net.InetAddress;
import java.net.UnknownHostException;

public class HelloWorld{
	//empty constructor is required by Proactive
	public HelloWorld(){
	}
	// the method returns StringWrapper so the calls can be ansychronous	
	public StringWrapper sayHello()
	{
		String hostname="Unkown";
		try {
			hostname=InetAddress.getLocalHost().toString();
		}
		catch (UnknownHostException excep){
			//hostname will be "Unknown"
			System.err.println(excep.getMessage());
		}
		return new StringWrapper("Distributed Hello! from " + hostname);
	}
}
 