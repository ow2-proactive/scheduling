package org.ow2.proactive_grid_cloud_portal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;


@Path("/a")
public class Test {

    /**
     * curl -H "Accept: text/plain" http://localhost:8080/proactive_grid_cloud_portal/a  
     * @return
     */

    @GET
    @Produces("text/plain")
    public String sayHello() {
        return "hello";
    }

    /**
     * curl -H "Accept: text/html" http://localhost:8080/proactive_grid_cloud_portal/a 
     * @return
     */
    @GET
    @Produces("text/html")
    public String sayHelloHtml() {
        return "<html><body>hello</body></html>";
    }

}
