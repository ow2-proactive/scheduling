/*
 * Created on Aug 20, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package testsuite.bean;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/** <p>To initialize some parameters of a class from a properties file.</p>
 * <p>Example :</p>
 * <pre>
 * public class Toto {
 *    private String hostname = "localhost";
 *    private int port = 8802;
 *
 *    public void setHostname(String hostname){
 *        this.hostname = hostname;
 *    }
 *
 *    public void setPort(String port){
 *        this.port = Integer.parseInt(port);
 *    }
 * }
 *
 * In properties file :
 *
 * port=8888
 * hostname=toto
 * </pre>
 *
 * @author Alexandre di Costanzo
 */
public interface Beanable {
	
	public void loadAttributes() throws IOException;
	
	public void loadAttributes(File propsFile) throws IOException;

	public void loadAttributes(Properties properties);
	
}
