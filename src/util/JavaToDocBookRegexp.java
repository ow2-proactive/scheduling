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

/** JavaToDocBook converter, in pure java, using regular expression replacement.
 * It converts a String, representing a file of java code, into another String, 
 * which represents a new file with docbook highlighting tags. */
public class JavaToDocBookRegexp implements JavaToDocBook {
    
    private static final String 
    SPAN="<emphasis role=",
    OPENCOMMENT = SPAN+"\"comment\">" , 
    CLOSECOMMENT = "</emphasis>", 
    OPENKEYWORD = SPAN+"\"keyword\">" ,
    CLOSEKEY = CLOSECOMMENT,
    OPENCODE = SPAN+"\"codeword\">",
    CLOSECODE = CLOSECOMMENT,
    OPENTYPE = SPAN+"\"typeword\">",
    CLOSETYPE = CLOSECOMMENT, 
    OPENSTRING = SPAN+"\"string\">",
    CLOSESTRING = CLOSECOMMENT, 
    EOL = "\n";
    
    static String genKeywords = "public|static|private|import|package|class|interface|implements|extends";
    static String codeKeywords = "return|this|if|else|for|while|do|break|continue|new|assert";
    static String typeKeywords = "void|null|int|String|Object";
    
    private boolean inComment = false;   
    private boolean inString = false;   
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
/*        if (this.inString) {
            int index = string.indexOf("\"");
            if (index >= 0)
                if (index == 0 || string.charAt(index-1) != '\\')  {
                    echo(string.substring(0,index + "\"".length()) + CLOSESTRING);
                    this.inString = false;
                    decorate (string.substring(index + "\"".length()));
                }
                else {     
                    echo(string.substring(0,index + "\"".length()) );
                    decorate (string.substring(index + "\"".length()));
                }
            else
                echo(string);
        }
        else 
*/            if (this.inComment) {
                int index = string.indexOf("*/");
                if (index >=0)  {
                    echo(string.substring(0,index + "*/".length()) + CLOSECOMMENT);
                    this.inComment = false;
                    decorate (string.substring(index + "*/".length()));
                }
                else
                    echo(string);
            }
            else {
  /*              int openQuote =  string.indexOf("\"");
                if (openQuote >=0)  {                       
                    decorate(string.substring(0,openQuote));
                    echo(OPENSTRING + "\"" );
                    inString = true;
                    decorate(string.substring(openQuote + "\"".length()));
                    return;
                }
    */            
                int lineComment = string.indexOf("//");       // hey, this is a comment
                if (lineComment >=0)  {                       
                    decorate(string.substring(0,lineComment));
                    echo(OPENCOMMENT + string.substring(lineComment) + CLOSECOMMENT);
                    return;
                }
                int index = string.indexOf("/*");
                if (index >=0)  {
                    decorate (string.substring(0,index ));
                    echo(OPENCOMMENT + "/*") ;
                    inComment=true;
                    decorate (string.substring(index +"/*".length()));
                }
                else {
                    String result = string.replaceAll("("+genKeywords+")" , OPENKEYWORD + "$0" + CLOSEKEY );
                    result = result.replaceAll("("+codeKeywords+")" , OPENCODE + "$0" + CLOSECODE );
                    result = result.replaceAll("(\\W)("+typeKeywords+")(\\W)" , "$1"+OPENTYPE + "$2" + CLOSETYPE +"$3" );
                    echo(result);
                }
            }
    }
    
    private void echo(String s)  throws IOException {
      tmpBuffer.write(s.replaceAll("\n", EOL));
    }
    

   /** Check that this can really work.
    * @return true in all cases, because this is java based word replacement  */
    public boolean willWork() {
      return true;
    }

    
}
