package org.objectweb.proactive.core.config.xml;

import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.xml.sax.SAXException;

public class MasterFileHandler extends AbstractUnmarshallerDecorator
    implements MasterFileConstants {

    static {
        BasicConfigurator.configure();
    }

    protected static ProActiveConfiguration config;

    public MasterFileHandler() {
        addHandler(PROPERTIES_TAG, new PropertiesHandler(MasterFileHandler.config));
		addHandler(LOG4J_FILE_TAG, new Log4jConfigurationHandler());
    }

    /**
     * Create a SAX parser on the specified file
     * @param filename the full path to the file
     */
    public static void createMasterFileHandler(String filename,
        ProActiveConfiguration config) {
			MasterFileHandler.config = config;
        InitialHandler h = new InitialHandler();
        org.objectweb.proactive.core.xml.io.StreamReader sr;
        try {
            sr = new org.objectweb.proactive.core.xml.io.StreamReader(new org.xml.sax.InputSource(
                        filename), h);
            sr.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws SAXException {
     //   System.out.println("End active handler");
    }

    public Object getResultObject() throws SAXException {
      //  System.out.println("get result object");
        return null;
    }

    public void startContextElement(String name, Attributes attributes)
        throws SAXException {
    }

    //
    // -- INNER CLASSES ------------------------------------------------------
    //
    private static class InitialHandler extends AbstractUnmarshallerDecorator {
        private MasterFileHandler masterFileHandler;

        // private InitialHandler(AbstractManager manager) {
        private InitialHandler() {
            super();
            masterFileHandler = new MasterFileHandler();
            //			  managerDescriptorHandler = new ManagerDescriptorHandler(manager);
            this.addHandler(MASTER_TAG, masterFileHandler);
        }

        public Object getResultObject() throws org.xml.sax.SAXException {
            //	  return managerDescriptorHandler;
            return null; //masterFileHandler;
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
        }

        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
        }
    }

    //    private class SingleValueUnmarshaller extends AbstractUnmarshallerDecorator {
    //        public void readValue(String value) throws org.xml.sax.SAXException {
    //            //  setResultObject(value);
    //        }
    //
    //        public void startContextElement(String name, Attributes attributes)
    //            throws org.xml.sax.SAXException {
    //            //	String key = attributes.getValue("key");
    //            String value = attributes.getValue("value");
    //            System.out.println("name = " + name + " value = " + value);
    //            //			if (checkNonEmpty(key) && checkNonEmpty(value)) {
    //            //				properties.put(key, value);
    //            //			}
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
    //         */
    //        protected void notifyEndActiveHandler(String name,
    //            UnmarshallerHandler activeHandler) throws SAXException {
    //            // TODO Auto-generated method stub
    //        }
    //
    //        /* (non-Javadoc)
    //         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
    //         */
    //        public Object getResultObject() throws SAXException {
    //            // TODO Auto-generated method stub
    //            return null;
    //        }
    //    }
}
