package org.objectweb.proactive.ext.webservices.utils;

import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.objectweb.proactive.core.xml.io.StreamReader;
import org.objectweb.proactive.ext.webservices.utils.ProActiveXMLUtils;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;


public class ProActiveXMLParser extends AbstractUnmarshallerDecorator {
    private InputStream in = null;
    private InitialHandler h;

    public ProActiveXMLParser(InputStream in) throws IOException {
        this.h = new InitialHandler();
        StreamReader sr = new org.objectweb.proactive.core.xml.io.StreamReader(new InputSource(
                    in), h);
        
        sr.read();
    
    }

    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws SAXException {
    }

    public Object getResultObject() throws SAXException {
        return (Object[]) h.getResultObject();
    }

    public void startContextElement(String name, Attributes attributes)
        throws SAXException {
    }

    //inner classes
    private class InitialHandler extends AbstractUnmarshallerDecorator {
        private Object[] result;

        private InitialHandler() {
            this.addHandler(ProActiveXMLUtils.PROACTIVE_MESSAGE,
                new MessageHandler());
        }

        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            this.result = (Object[]) activeHandler.getResultObject();
        }

        public Object getResultObject() throws SAXException {
            return result;
        }

        public void startContextElement(String name, Attributes attributes)
            throws SAXException {
        }
    }

    /************************************************************************************************************************/
    private class MessageHandler extends AbstractUnmarshallerDecorator {
        private Object[] result;

        private MessageHandler() {
            addHandler(ProActiveXMLUtils.PROACTIVE_ACTION, new ActionHandler());
        }

        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            this.result = (Object[]) activeHandler.getResultObject();
        }

        public Object getResultObject() throws SAXException {
            return this.result;
        }

        public void startContextElement(String name, Attributes attributes)
            throws SAXException {
        }
    }

    private class SingleValueUnmarshaller extends BasicUnmarshaller {
        String result = "";

        public void readValue(String value) throws org.xml.sax.SAXException {
            result += value;
            setResultObject(result);
        }
    }

    /************************************************************************************************************************/
    private class ActionHandler extends AbstractUnmarshallerDecorator {
        Object[] result = new Object[2];

        private ActionHandler() {
            addHandler(ProActiveXMLUtils.PROACTIVE_OBJECT,
                new SingleValueUnmarshaller());
            addHandler(ProActiveXMLUtils.PROACTIVE_OAID,
                new SingleValueUnmarshaller());
        }

        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            if (name.equals(ProActiveXMLUtils.PROACTIVE_OBJECT)) {
                Object o = activeHandler.getResultObject();
                result[1] = o;
            } 
        }

        public Object getResultObject() throws SAXException {
            return result;
        }

        public void startContextElement(String name, Attributes attributes)
            throws SAXException {
            result[0] = (String) attributes.getValue("name");
        }
    }
}
