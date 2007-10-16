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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** Adding docbook highlighting tags through regular expressionn replacement. */
public abstract class RegexpHighLighter implements LanguageToDocBook {
    protected static int MAX = 100; // can be overridden if you want line break to happen on shorter/longer lines
    protected static Pattern pat = Pattern.compile("<.*?>"); // this regexp represents docbook tags in the line
    private StringBuffer resultBuffer; // the characters eventually returned are stored here
    protected Pattern[] pattern; // the pattern to match
    protected String[] replacement; // the replacement for the patterns

    /** Add tags around keywords in the given string, depending on the language.
     * @return the initial string with xml tags highlighting parts of it */
    protected abstract String decorate(String string);

    /** Reset the logic of the highlighter, when opening a new file. */
    protected abstract void reset();

    /** Print to the output stream the line just highligthed.
     *  Extra treatment: cut too long lines! */
    protected void echo(String s) {
        String split = split(s);
        this.resultBuffer.append(split);
    }

    /** Check that this highlighter can really work; it always does for regexp.
     * @return true in all cases, because this is 100% java, regular expression-based word replacement  */
    public boolean willWork() {
        return true;
    }

    /** Convert the given code String into a String of docbook highlighted code.
     * @param codeString the bits of code to be highlighted
     * @return the initial string sprinkled with docbook tags */
    public String convert(String codeString) {
        this.resultBuffer = new StringBuffer();
        reset();

        // split the given code string into lines, which are each decorated in turn.   
        String[] lines = codeString.split("\n");

        for (int i = 0; i < lines.length; i++) {
            echo(decorate(lines[i]));
            echo("\n");
        }

        return this.resultBuffer.toString();
    }

    /** Splits long lines so they wrap. depends on the above two static variables.
     * @param string the line which is to be split if it is to long
     * @return the initial string if it is short enough. */
    protected String split(String string) {
        // THIS CODE COULD BE MADE MORE EFFICIENT & CLEANER WITH JUST '<' ' ' SEARCH IN string. 
        if (string.length() < MAX) { // code tuning 

            return string;
        }

        String visible = pat.matcher(string).replaceAll("");

        if (visible.length() < MAX) {
            return string;
        }

        // Only run if string is too long! 
        Matcher mat = pat.matcher(string);
        int visibleLenght = 0;
        int groupLength = 0;
        int previousGroupEnd = 0;
        int start = 0;

        do {
            previousGroupEnd = start + groupLength; // copy the previous position 

            if (mat.find()) { // can a new tag be found ?
                start = mat.start();
                groupLength = mat.group().length();
                visibleLenght += (start - previousGroupEnd); // the length of the current visible string 
            } else { // no more tags found, but (previous) visibleLength < MAX 
                start = string.length();
                visibleLenght += (start - previousGroupEnd);

                if (visibleLenght < MAX) { // OK, found the end of string, and it's short enough: don't cut

                    return string;
                }

                // if >= MAX, we'll get out of the do-loop, and same treatment applies.
            }
        } while (visibleLenght < MAX);

        // OK, the string read is too long. 
        visibleLenght -= (start - previousGroupEnd);

        String tosplit = string.substring(previousGroupEnd,
                (MAX - visibleLenght) + previousGroupEnd);

        // the bit (possibly between tags) which really has to be split
        // We need to split so that visibleLength < MAX, which is done by above tosplit length. 
        int index = tosplit.lastIndexOf(' ');

        if (index < 1) { // did we find a possible breaking spot? 

            if (previousGroupEnd == 0) {
                System.err.println("Warning: had to insert hard line break in " +
                    tosplit);
                previousGroupEnd = MAX; // define cut at longest allowed line length
            }

            index = 0;
        }

        //index+ previousGroupStart is the place where the string should be split!  
        return string.substring(0, index + previousGroupEnd) + '\n' +
        split(string.substring(index + previousGroupEnd));
    }
}
