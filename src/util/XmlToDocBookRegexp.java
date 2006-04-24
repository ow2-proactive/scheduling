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
package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

/** JavaToDocBook converter (100% java), using regular expression replacement.
 * It converts a String, pointing to a file of java code, into another String, 
 * which in turn points to a new file containing the java code plus docbook highlighting tags. */
public class XmlToDocBookRegexp implements LanguageToDocBook {
            
    private static String XML_COMMENT_START = "<!--";
    private static String XML_COMMENT_END = "-->";
    
    //   \b in a regexp means a word boundary                     
    private static Pattern [] pattern = {
        Pattern.compile("(\\s)(\\w*)(=)(\".*?\")()"), // the attributes construct, like: role="toto"
        Pattern.compile("(&lt;/?)(\\w*)(\\b)"),   // the tag name, opening or ending
        //Pattern.compile("("+XML_COMMENT_START+")(.*)("+XML_COMMENT_END+")"),   //  <!-- comment --> 
    }; 
    private static String [] replacement = {                    
        "$1"+OPENTYPE+ "$2" +CLOSETYPE+"$3" + OPENSTRING + "$4" + CLOSESTRING + "$5",
        "$1"+OPENKEYWORD+ "$2" +CLOSEKEY+"$3" ,
       // "$1"+OPENCOMMENT+ "$2" +CLOSECOMMENT+"$3" ,
    }; 

    
    // When we're in a coment, nothing else matters. 
    private boolean inComment = false;   
    // You can insert directly docbook tags to highlight your example code. 
    private String docBookTag = null;
     
    private BufferedWriter tmpBuffer;

    /** Convert the given file into docbook highlighted code.
     * @param path the place from where should be loaded the file to convert
     * @return the file which is created, with highlighted tags. 
     */
    public String convert(String path) throws IOException {

        // create file in which we shall output the code.
        File temp = new File(path + ".xml");
        tmpBuffer = new BufferedWriter(new FileWriter(temp));

        // read all strings from given file, and decorate
        BufferedReader in = new BufferedReader(new FileReader(path));
        String str;
        while ((str = in.readLine()) != null) {
            //decorate(str.replaceAll(" ", "&nbsp;")+"\n");
           decorate(str+"\n");
        }

        in.close();
        tmpBuffer.close();

        return path+".xml";
    }
    
    // Add tags around java keywords
    private void decorate(String string)  throws IOException {
        if (this.docBookTag != null) {
            int index = string.indexOf(this.docBookTag);
            if (index >=0)  {
                echo(string.substring(0,index + this.docBookTag.length()) );
                index += this.docBookTag.length();
                this.docBookTag = null;
                decorate (string.substring(index ));
            }
            else 
                echo(string);
         }else
        
        if (this.inComment) {
                int index = string.indexOf(XML_COMMENT_END);
                if (index >=0)  {
                    echo(string.substring(0,index + XML_COMMENT_END.length()) + CLOSECOMMENT);
                    this.inComment = false;
                    decorate (string.substring(index + XML_COMMENT_END.length()));
                }
                else
                    echo(string);
            }
            else {
                // If start of a real docbook tag, annihilate any other processing. 
                int ind = string.indexOf("<");
                if (ind >=0)  {
                    decorate (string.substring(0,ind ));
                    this.docBookTag=string.substring(ind+1).replaceAll("(\\w*)(\\b.*)", "$1").replaceAll("\n","");
                    echo("<" + this.docBookTag );
                    ind += ("<" + this.docBookTag).length();
                    this.docBookTag = "</"+ this.docBookTag+ ">";
                    
                    decorate (string.substring(ind) );
                }
                else {
                // If start of a comment find closing tag  
                int index = string.indexOf(XML_COMMENT_START);
                if (index >=0)  {
                    decorate (string.substring(0,index ));
                    echo(OPENCOMMENT + XML_COMMENT_START) ;
                    inComment=true;
                    decorate (string.substring(index +XML_COMMENT_START.length()));
                }
                else { 
                    String result = string;
                    // for all the patterns defined, do regexp replacement 
                    for (int i = 0 ; i < pattern.length; i++)
                        result = pattern[i].matcher(result).replaceAll(replacement[i]);
                    // pattern reuse is faster than String.replaceAll(regecp, replacement)
   
                    echo(result);
                }
            }
            }
    }
    
    private void echo(String s)  throws IOException {
      tmpBuffer.write(s.replaceAll("\n", EOL));
    }
    

   /** Check that this can really work.
    * @return true in all cases, because this is 100% java, regular expression-based word replacement  */
    public boolean willWork() {
      return true;
    }

    
}
