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

    /**
     * To load attributes from a default properties file.
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    public void loadAttributes() throws IOException;

    /**
     * To load attributes from a specified properties file.
     * @param propsFile the properties file.
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    public void loadAttributes(File propsFile) throws IOException;

    /**
     * To load attrobutes from Java properties.
     * @see java.util.Properties
     * @param properties Java properties.
     */
    public void loadAttributes(Properties properties);
}
