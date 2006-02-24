package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This works this way:
 * Some kind of ugly sax parsing is done on the xml file, spitting out the tags as they come, 
 * until a tag <programmlisting> or <screen> is found. The data inside the tags is then 
 * highlighted by the method highlight(String).  It is then fed back into the stream. 
 */
public class DocBookize extends DefaultHandler
{
    final static boolean DEBUG = false;
    private final String FILEPATH;

    public static void main(String argv[])
    {
        // usage warning
        if (argv.length != 2 && argv.length != 1) {
            System.err.println("Usage: docBookize filename pathForTextData");
            System.err.println("Transforms <screen> and <programlisting> tags (side-effect: removes docbook indentation)");
            System.exit(-1);
        }
        System.out.println("Beautifying code examples within [programlisting] tags in " + argv[0]);
                
        // Create SAX machinery 
        File inputFile = new File(argv[0]);
        // if no pathForTextData specified on command line, default is ""
        DefaultHandler handler = new DocBookize(argv[0] + ".tmp" , (argv.length ==2 ? argv[1] : "") );
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = factory.newSAXParser();
            // parse the file, using DocBookize.methods to handle tags
            saxParser.parse( inputFile, handler);
            
            // rename output file to first name ==> overwrite
            File resultFile = new File(argv[0] + ".tmp");
            resultFile.renameTo(inputFile);
            
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    private boolean storingJavaCode = false;
    private String programContent ;
    private BufferedWriter out;
    private String outputFileName;
    private String language = "";
    private JavaToDocBook javaToDocBook;
    
    public DocBookize(String fileName, String path) {
        this.outputFileName = fileName ;
        FILEPATH = path;
        this.javaToDocBook = getConverter();
        if (this.javaToDocBook==null)
            throw new NullPointerException("No converter found, code will not be hightlighted");
    }
    
    public void startDocument()
    throws SAXException
    {
        
        // Create the output file
        try {
            out = new BufferedWriter(new FileWriter(this.outputFileName));
        } catch (IOException e) {
            throw new SAXException("I/O error opening temp file", e);
        }
        
        print("<?xml version='1.0' encoding='UTF-8'?>\n");
        print("<!-- DocBookize has been run to highlight keywords in examples -->\n");
    }
    
    public void endDocument()
    throws SAXException
    {
        try {
            out.close();
        } catch (IOException e) {
            throw new SAXException("I/O error closing temp file", e);
        }
    }
    
    public void startElement(String namespaceURI, String localName,  String realName, Attributes attrs)
    throws SAXException
    {
        String tagName = localName; // element name
        if ("".equals(tagName)) tagName = realName; // namespaceAware = false
        comment("TAG  " + tagName);
        
        // Just skip textobjects in programlistings (see following comment).
        if (tagName.equals("textobject") && storingJavaCode) 
            return;

        // replace textdata from file by the file content (xml parser should already do that, but fails. ant xslt task bugged?)
        if (tagName.equals("textdata") && storingJavaCode) {
            String fileReferenced = "";
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name 
                if ("".equals(aName)) aName = attrs.getQName(i);
                if (aName.equals("fileref"))
                    fileReferenced=attrs.getValue(i);            
            }
            programContent += getFileContent(fileReferenced);
            return; 
        }

        print("<"+tagName);
        
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name 
                if ("".equals(aName)) aName = attrs.getQName(i);
                
                comment("   ATTR: ");
                print(' ' +aName);
                print("=\"");
                print(attrs.getValue(i));
                if (aName.toLowerCase().equals("language"))
                    language = attrs.getValue(i).toLowerCase();
                print("\" ");
            }
        }
        print(">");
        // Only highlighting java programlistings right now.
        if ((tagName.toLowerCase().equals("programlisting") && language.equals("java") ) ) {
            storingJavaCode=true;
            programContent="";
        }
    }
    
    private String getFileContent(String fileReferenced) {
        String fileContent = "";
        try {
            BufferedReader in = new BufferedReader(new FileReader(FILEPATH + fileReferenced));

            // Remove PA standard java file header, if any
            String str1 = in.readLine();
            if (str1 == null) {in.close(); return "";}
            String str2 = in.readLine();
            if (str2 == null) {in.close(); return str1;}
            if (str2.startsWith(" * ################################################################"))
                // begin PA comment, so just read it until end comment found
                do {
                    str2 = in.readLine();
                    // if EndOfFile, just return with empty String
                    if (str2==null) return "";
                }
                while (!str2.endsWith("*/")); 
            
            // just dump rest of the file into return value
            String str;
            while ((str = in.readLine()) != null) {
                fileContent += str + "\n";
            }
            in.close();
        } catch (IOException e) { 
                System.err.println("Warning : trouble reading referenced file : fileReferenced");
                System.err.println(e.getMessage()); 
                }
    return fileContent;    
    }

    public void endElement(String namespaceURI, String localName, String realName )
    throws SAXException
    {
        comment("END_TAG: ");
        // Just skip textobjects in programlistings. 
        if (realName.toLowerCase().equals("textobject") && storingJavaCode) 
            return;
        // Just skip textdata END TAGS in programlistings. 
        if (realName.toLowerCase().equals("textdata") && storingJavaCode) 
            return;
        if (realName.toLowerCase().equals("programlisting") ) {
            storingJavaCode=false;
            highlight (programContent);
            programContent="";
        }
        print("</"+realName+">");
        
    }
    
    public void characters(char buf[], int offset, int len)
    throws SAXException
    {
        comment("CHAR:   ");
        String s = new String(buf, offset, len);
//        if (storingJavaCode)
//            print(s);
//        else
            print(s.replaceAll("&", "&amp;").replaceAll("<", "&lt;") );
    }
    
    /** Just write to the stream the given string
     * @throws SAXException need to Wrap I/O exceptions in 
     * SAX exceptions, to suit handler signature requirements
     */
    private void print(String s)
    throws SAXException
    {
        if (storingJavaCode )
            programContent += s ;
        else 
            try {
                if (DEBUG)
                    System.out.print(s);
                out.write(s);
            } catch (IOException e) {
                throw new SAXException("I/O error writing to temp file", e);
            }
    }
    
//  Debugging method TODO : remove  
    private void comment(String s)
    {
        if (DEBUG)
            System.out.println(s);
    }
    
    
    /** Transform the given string into nice docbook highlighted stuff
     * @param s The String which is to contain tags highlighting its elements.
     * @throws SAXException if writing to the stream caused problem 
     */
    private void highlight(String s)
    throws SAXException
    {
        if (s.length() > 0) 
            try {
                // First create the docbookized version of this java code : 
                File temp = File.createTempFile("db_tmp_", ".java");
                BufferedWriter tmpBuffer = new BufferedWriter(new FileWriter(temp));
                tmpBuffer.write(s);
                tmpBuffer.close();
                String generated = javaToDocBook.convert(temp.getPath());
                temp.delete();

                // now put this code back into the xml we're generating
                BufferedReader in = new BufferedReader(new FileReader(generated));
                String str;
                while ((str = in.readLine()) != null) {
                    print(str+"\n");
                }
                in.close();
                //new File(generated).delete();
            } catch (IOException e) {
                throw new SAXException("I/O error writing to temp file", e);
            }
    }
    
    /** Gets a JAVATODOCBOOK conversion class, acording to the machine possibilities.
     * @return a JavaToDocBook instance, which can be used to transform java to DocBook */
    private JavaToDocBook getConverter() {
        String [] classes = { "util.JavaToDocBookExternal", "util.JavaToDocBookRegexp", } ; 
        JavaToDocBook instance = null;
        for (int i = 0 ; i < classes.length; i++) {
            try {
                // create the class. 
                instance = (JavaToDocBook) Class.forName(classes[i]).newInstance();
            } catch (Exception e) {// pb loading this class, well, just try another one!
                continue ;
            }
            if (instance.willWork() )  // class can only be used if it exists, and it will do the conversion..
                break;
            // if instance won't work, forget about it.
            instance = null;
        }
        return instance; 
    }
    
    
}
