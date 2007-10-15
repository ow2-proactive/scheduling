package active;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

public class HelloWorld {
	public HelloWorld(){//empty constructor is required by ProActive
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
//		((InitializedHelloWorld) ProActive.getStubOnThis()).internal();
		return new StringWrapper("Distributed Hello! from " + hostname);
	}
}
 