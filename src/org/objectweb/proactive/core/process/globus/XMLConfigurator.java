

package org.objectweb.proactive.core.process.globus;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * This class parse the globus.xml file and create an Hmap of GlobusHostInfos
 */

public class XMLConfigurator extends DefaultHandler
{

    protected Dictionary globusHostsInfos=new Hashtable();    

    protected static final int ROOT=0,
			       INFOS=1,
			       HOST=2,
			       NAME=3,
			       JAVAHOME=4,
			       PROACTIVEHOME=6,
			       STDOUT=9,
			       GRAMPORT=10,
			       GISPORT=11;
    protected int state;

    protected StringBuffer textBuffer;

    protected String hostName,
		     javaHome,
    		     proactivehome,
		     stdout,
   		     gramport,
		     gisport;
    

    static private Writer  out;

    //===========================================================
    // Constructor
    //===========================================================

    public XMLConfigurator(){

        // Use an instance of ourselves as the SAX event handler
        DefaultHandler handler = this;

        // Use the default (non-validating) parser
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            // Set up output stream
            out = new OutputStreamWriter(System.out, "UTF8");

            // Parse the input
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse( new File("globus.xml"), handler);

        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    //===========================================================
    // Important method
    //===========================================================

    public Dictionary getGlobusConfiguration(){

      return globusHostsInfos;

    }

    //===========================================================
    // SAX DocumentHandler methods
    //===========================================================

    public void startDocument()
    throws SAXException
    {
	state=ROOT;
    }

    public void endDocument()
    throws SAXException
    {
        try {
            //nl();
            out.flush();
	    //this.DisplayAllGlobusHostsInfos();
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }

    public void startElement(String namespaceURI,
                             String sName, // simple name
                             String qName, // qualified name
                             Attributes attrs)
    throws SAXException
    {
	if(qName.equals("infos") && state==ROOT){
	  state=INFOS;
	}
	if(qName.equals("host") && state==INFOS){
	  state=HOST;
	}
        else if (qName.equals("name") && state==HOST){
	  state=NAME;
        }
	else if (qName.equals("javaHome") && state==HOST){
	  state=JAVAHOME;
        }
	else if (qName.equals("ProActiveHome")  && state==HOST){
	  state=PROACTIVEHOME;
        }
	else if (qName.equals("stdOut") && state==HOST){
	  state=STDOUT;	  
        }
	else if (qName.equals("GramPort")  && state==HOST){
	  state=GRAMPORT;
        }
	else if (qName.equals("GISPort") && state==HOST){
	  state=GISPORT;
        }

    }

    public void endElement(String namespaceURI,
                           String sName, // simple name
                           String qName  // qualified name
                          )
    throws SAXException
    {
	if(qName.equals("infos") && state==INFOS){
	  state=ROOT;
	}
	if(qName.equals("host") && state==HOST){
	  state=INFOS;

          // Initialise the GlobusHostInfo and put it the Dictionary
	  GlobusHostInfos ghi=new GlobusHostInfos(hostName,
						  javaHome,
						  proactivehome,
						  stdout,
						  gramport,
						  gisport);
	  //We update the Hmap
	  globusHostsInfos.put(ghi.GetHostName(),ghi);						  
	}
        else if (qName.equals("name") && state==NAME){
	  hostName=textBuffer.toString();
	  hostName=DraftAttribute(hostName);
	  state=HOST;
        }
	else if (qName.equals("javaHome") && state==JAVAHOME ){
	  javaHome=textBuffer.toString();
	  javaHome=DraftAttribute(javaHome);
	  state=HOST;
        }
	else if (qName.equals("ProActiveHome") && state==PROACTIVEHOME){
	  proactivehome=textBuffer.toString();
	  proactivehome=DraftAttribute(proactivehome);
	  state=HOST;
        }
	else if (qName.equals("stdOut") && state==STDOUT){
	  stdout=textBuffer.toString();
	  stdout=DraftAttribute(stdout);
	  state=HOST;	  
        }
	else if (qName.equals("GramPort") && state==GRAMPORT){
	  gramport=textBuffer.toString();
	  gramport=DraftAttribute(gramport);
	  state=HOST;
        }
	else if (qName.equals("GISPort") && state==GISPORT){
	  gisport=textBuffer.toString();
	  gisport=DraftAttribute(gisport);
	  state=HOST;
        }

	textBuffer = null;

    }

    public void characters(char buf[], int offset, int len)
    throws SAXException
    {
        String s = new String(buf, offset, len);
        if (textBuffer == null) {
           textBuffer = new StringBuffer(s);
        } 
	else {
           textBuffer.append(s);
        }
    }
    
    //===========================================================
    // Utility Methods ...
    //===========================================================

    //Draft a string and remove all '\n' and ' ' characters
    private String DraftAttribute(String aString){

	  int l=aString.length();
	  StringBuffer sb=new StringBuffer();	

          for(int i=0;i<l;i++){
            if ((aString.charAt(i)=='\n') || (aString.charAt(i)==' ')){
	    }
            else{
               sb.append(aString.charAt(i));
            }
	  }

          return sb.toString();
      	
    }
    


    //Display all the infos about the globus host contain in the dictionnary
    public void DisplayAllGlobusHostsInfos(){

      Enumeration keys = globusHostsInfos.keys();
      while(keys.hasMoreElements()){
	String key = (String) keys.nextElement();
	GlobusHostInfos ghiTemp = (GlobusHostInfos) globusHostsInfos.get(key);
	ghiTemp.DisplayYourSelf();
      }

    }
    
    // Display text accumulated in the character buffer
    private void echoText()
    throws SAXException
    {
        if (textBuffer == null) return;
        String s = ""+textBuffer;
        emit(s);
        textBuffer = null;
    }

    // Wrap I/O exceptions in SAX exceptions, to
    // suit handler signature requirements
    private void emit(String s)
    throws SAXException
    {
        try {
            out.write(s);
            out.flush();
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }

    // Start a new line
    private void nl()
    throws SAXException
    {
        String lineEnd =  System.getProperty("line.separator");
        try {
            out.write(lineEnd);
        } catch (IOException e) {
            throw new SAXException("I/O error", e);
        }
    }

    public static void main(String argv[]){

      XMLConfigurator xc = new XMLConfigurator();

    }


}
