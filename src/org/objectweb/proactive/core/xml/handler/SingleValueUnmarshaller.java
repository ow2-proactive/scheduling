package org.objectweb.proactive.core.xml.handler;

import org.xml.sax.SAXException;

/**
 * A handler for reading values from simple elements, such as 
 * <pre><myElement>myValue</myElement></pre>
 * 
 * @author Matthieu Morel
 *
 */

public class SingleValueUnmarshaller extends BasicUnmarshaller {
    
    /**
     * The implementation of this method ensures that even though the element value is split into several chunks, 
     * we concatenate the chunks to build the actual value.
     * see http://www.saxproject.org/faq.html (The ContentHandler.characters() callback is missing data!)
     * and http://xml.apache.org/xerces2-j/faq-sax.html#faq-2
     * This method is called several times by {@link org.objectweb.proactive.core.xml.io.DefaultHandlerAdapter#characters(char[], int, int)}
     * if the data is split into several chunks.
     */
    public void readValue(String value) throws SAXException {
        if (resultObject == null) {
            setResultObject(value);
        } else {
            setResultObject(resultObject +value );
        }
    }
}