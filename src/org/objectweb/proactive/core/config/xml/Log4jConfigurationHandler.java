package org.objectweb.proactive.core.config.xml;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.PropertyConfigurator;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;


public class Log4jConfigurationHandler extends AbstractUnmarshallerDecorator
    implements MasterFileConstants {
    public Log4jConfigurationHandler() {
        super();
    }

    public Object getResultObject() throws org.xml.sax.SAXException {
        return null;
    }

    public void startContextElement(String name, Attributes attributes)
        throws org.xml.sax.SAXException {
        String file = attributes.getValue("file");
      //  System.out.println("file is " + file);
      //we check if there is an already defined property
//      if (System.getProperty("log4j.configuration") != null) {
//      	System.out.println("Already defined configuration for log4j");
//		System.out.println(System.getProperty("log4j.configuration"));
//		PropertyConfigurator.configure(System.getProperty("log4j.configuration"));
//      	
//      } else {
//      
        PropertyConfigurator.configure(relativeToAbsolute(file));
    //  }
	 // System.out.println(ProActiveConfiguration.class.getResource("ProActiveConfiguration.xml"));
	//	String s = "jar:file:/home1/fabrice/workProActive/ProActive/dist/ProActive/ProActive.jar!/org/objectweb/proactive/core/config/../../../../../../scripts/unix/proactive-log4j";
	//	System.out.println(s);
	//	URL u = null;
	//	try {
	//		u = new URL(s);
	//	} catch (MalformedURLException e) {
	//		// TODO Auto-generated catch block
	//		e.printStackTrace();
	//	}
	//	PropertyConfigurator.configure(u);
	
    }

    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
    }

    protected String relativeToAbsolute(String path) {
        String s2 = path;
//System.out.println("Log4jConfigurationHandler path " + path);
        //this test doesn't look good
        //maybe we should rather try to open the file using the path
        //and convert it if we cannot
        if (path.indexOf("/") != 0) {
            //we have a relative path, we should turn it into 
            //an absolute one
			URL url = null;
			try {
				url = new URL("file:" + path);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} 
		//	System.out.println("Log4jConfigurationHandler url " + url);
            String s = ProActiveConfiguration.class.getResource("/" +
                    ProActiveConfiguration.class.getName().replace('.', '/') +
                    ".class").getPath();
            s2 = s.replaceAll("ProActiveConfiguration.class", path);
			//return url.getPath();
        }

        return s2;
    }
}
