/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package doc;

import java.util.regex.Pattern;


/** JavaToDocBook converter (100% java), using regular expression replacement.
 * It converts a String of java code, into another String containing
 * the java code plus docbook highlighting tags. */
public class XmlToDocBookRegexp extends RegexpHighLighter {
    private static final String XML_COMMENT_START = "&lt;!--";
    private static final String XML_COMMENT_END = "-->";

    // When we're in a coment, nothing else matters. 
    private boolean inComment;

    // You can insert directly docbook tags to highlight your example code. 
    private String docBookTag;

    public XmlToDocBookRegexp() {
        pattern = new Pattern[] { //   \b in a regexp means a word boundary

        Pattern.compile("(\\t)"), // Replace all tabs 
                Pattern.compile("(\\s)(\\w*)(=)(\".*?\")()"), // the attributes construct, like: role="toto"
                Pattern.compile("(&lt;/?)([a-zA-Z_0-9:-]*)(\\b)"), // the tag name, opening or ending
        };
        replacement = new String[] {
                "  ", // replacement for a tab is two spaces
                "$1" + OPENTYPE + "$2" + CLOSETYPE + "$3" + OPENSTRING + "$4" + CLOSESTRING + "$5",
                "$1" + OPENKEYWORD + "$2" + CLOSEKEY + "$3", };
        reset();
    }

    /** Add tags around xml constructs in the given string
     * @return the initial string with xml tags highlighting parts of it */
    @Override
    protected String decorate(String xmlString) {
        String result = xmlString; // the returned highlighted string

        if (this.docBookTag != null) {
            int index = xmlString.indexOf(this.docBookTag);

            if (index >= 0) {
                result = xmlString.substring(0, index + this.docBookTag.length());
                index += this.docBookTag.length();
                this.docBookTag = null;
                result += decorate(xmlString.substring(index));
            }

            //else result = xmlString;  already set
        } else if (this.inComment) {
            int index = xmlString.indexOf(XML_COMMENT_END);

            if (index >= 0) {
                this.inComment = false;
                result = xmlString.substring(0, index + XML_COMMENT_END.length()) + CLOSECOMMENT;
                result += decorate(xmlString.substring(index + XML_COMMENT_END.length()));
            }

            //else result = xmlString;  already set
        } else {
            // If start of a real docbook tag, annihilate any other processing. 
            int ind = xmlString.indexOf("<");

            if (ind >= 0) {
                result = decorate(xmlString.substring(0, ind));
                this.docBookTag = xmlString.substring(ind + 1).replaceAll("(\\w*)(\\b.*)", "$1") // hide the tag parameters
                        .replaceAll("\n", "");
                result += ("<" + this.docBookTag);
                ind += ("<" + this.docBookTag).length();
                this.docBookTag = "</" + this.docBookTag + ">";
                result += decorate(xmlString.substring(ind));
            } else {
                // If start of a comment find closing tag  
                int index = xmlString.indexOf(XML_COMMENT_START);

                if (index >= 0) {
                    result = decorate(xmlString.substring(0, index));
                    result += (OPENCOMMENT + XML_COMMENT_START);
                    inComment = true;
                    result += decorate(xmlString.substring(index + XML_COMMENT_START.length()));
                } else {
                    //  result = xmlString; already set
                    // for all the patterns defined, do regexp replacement 
                    for (int i = 0; i < pattern.length; i++)
                        result = pattern[i].matcher(result).replaceAll(replacement[i]);

                    // pattern reuse is faster than String.replaceAll(regexp, replacement)
                }
            }
        }

        return result;
    }

    /** Put all fields back to inital value */
    @Override
    protected void reset() {
        this.inComment = false;
        this.docBookTag = null;
    }
}
