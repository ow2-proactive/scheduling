package org.objectweb.proactive.ext.webservices;

import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.ext.webservices.soap.ProActiveDeployer;


@PublicAPI
public final class WebServices {

    /**
     *  Expose an active object as a web service
     * @param o The object to expose as a web service
     * @param url The url of the host where the object will be seployed  (typically http://localhost:8080)
     * @param urn The name of the object
     * @param methods The methods that will be exposed as web services functionnalities
     */
    public static void exposeAsWebService(Object o, String url, String urn,
        String[] methods) {
        ProActiveDeployer.deploy(urn, url, o, methods);
    }

    /**
     * Delete the service on a web server
     * @param urn The name of the object
     * @param url The url of the web server
     */
    public static void unExposeAsWebService(String urn, String url) {
        ProActiveDeployer.undeploy(urn, url);
    }

    /**
     *  Deploy a component as a webservice. Each interface of the component will be accessible by
     * the urn [componentName]_[interfaceName]in order to identify the component an interface belongs to.
     * All the interfaces public  methods will be exposed.
     * @param componentName The name of the component
     * @param url  The web server url  where to deploy the service - typically "http://localhost:8080"
     * @param component The component owning the interfaces that will be deployed as web services.
     */
    public static void exposeComponentAsWebService(Component component,
        String url, String componentName) {
        ProActiveDeployer.deployComponent(componentName, url, component);
    }

    /**
     * Undeploy component interfaces on a web server
     * @param componentName The name of the component
     * @param url The url of the web server
     * @param component  The component owning the services interfaces
     */
    public static void unExposeComponentAsWebService(String componentName,
        String url, Component component) {
        ProActiveDeployer.undeployComponent(componentName, url, component);
    }
}
