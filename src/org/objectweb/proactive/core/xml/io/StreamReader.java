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
package org.objectweb.proactive.core.xml.io;

/**
 *
 * Implement an XLMReader based on SAX reading from a stream 
 *
 * @author       Lionel Mestre
 * @version      0.91
 *
 */
public class StreamReader implements XMLReader {
  
  private org.xml.sax.XMLReader parser;
  private org.xml.sax.InputSource inputSource;
  
  
  public StreamReader(java.io.InputStream in, XMLHandler xmlHandler) throws java.io.IOException {
    this(new org.xml.sax.InputSource(in), xmlHandler);
  }
  
  public StreamReader(java.io.Reader reader, XMLHandler xmlHandler) throws java.io.IOException {
    this(new org.xml.sax.InputSource(reader), xmlHandler);
  }
  
  public StreamReader(org.xml.sax.InputSource inputSource, XMLHandler xmlHandler) throws java.io.IOException {
    this.inputSource = inputSource;
    DefaultHandlerAdapter adaptor = new DefaultHandlerAdapter(xmlHandler);
    try {
    	javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
    	parser = factory.newSAXParser().getXMLReader();
      //parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
    	parser.setContentHandler(adaptor);
    	parser.setErrorHandler(adaptor);
    } catch (org.xml.sax.SAXException e) {
      throw new java.io.IOException(e.toString());
    } catch (javax.xml.parsers.ParserConfigurationException e) {
      throw new java.io.IOException(e.toString());
    }
  }


  
  // -- implements XMLReader ------------------------------------------------------
  
  public void read() throws java.io.IOException {
    try {
      parser.parse(inputSource);
    } catch (org.xml.sax.SAXException e) {
      e.printStackTrace();
      throw new java.io.IOException(e.toString());
    }
  }

}