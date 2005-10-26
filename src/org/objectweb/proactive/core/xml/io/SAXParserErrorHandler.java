package org.objectweb.proactive.core.xml.io;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


public class SAXParserErrorHandler extends DefaultHandler {
    static Logger logger = ProActiveLogger.getLogger(Loggers.XML);

    public SAXParserErrorHandler() {
    }

    public void warning(SAXParseException ex) throws SAXException {
        logger.warn("WARNING: " + ex.getMessage());
    }

    public void error(SAXParseException ex) throws SAXException {
        logger.error("ERROR: " + ex.getSystemId() + " Line:" +
            ex.getLineNumber() + " Message:" + ex.getMessage());
    }

    public void fatalError(SAXParseException ex) throws SAXException {
        logger.fatal("FATAL ERROR: " + ex.getMessage());
    }
}
