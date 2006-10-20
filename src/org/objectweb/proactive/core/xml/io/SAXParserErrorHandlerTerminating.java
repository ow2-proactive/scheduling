/**
 * 
 */
package org.objectweb.proactive.core.xml.io;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author fviale
 *
 */
public class SAXParserErrorHandlerTerminating extends SAXParserErrorHandler {
	public SAXParserErrorHandlerTerminating() {
	}
	
    public void warning(SAXParseException ex) throws SAXException {
        super.warning(ex);
    }

    public void error(SAXParseException ex) throws SAXException {
    	super.error(ex);
        throw new SAXException(ex.getMessage());
    }

    public void fatalError(SAXParseException ex) throws SAXException {
    	super.fatalError(ex);
        throw new SAXException(ex.getMessage());
    }

}
