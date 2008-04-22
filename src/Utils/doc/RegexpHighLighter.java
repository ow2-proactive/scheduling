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
    /** can be overridden if you want line break to happen on shorter/longer lines */
    private int maxLineLength = 100;
    // this regexp represents docbook tags in the line
    private final Pattern PAT = Pattern.compile("<.*?>");
    // the characters eventually returned are stored here
    private StringBuffer resultBuffer;
    // the pattern to match
    protected Pattern[] pattern;
    // the replacement for the patterns
    protected String[] replacement;

    /** Add tags around keywords in the given string, depending on the language.
     * @return the initial string with xml tags highlighting parts of it */
    protected abstract String decorate(String string);

    /** Reset the logic of the highlighter, when opening a new file. */
    protected abstract void reset();

    /** Print to the output stream the line just highligthed.
     *  Extra treatment: cut too long lines! */
    protected void echoToOutputStream(final String s) {
        final String split = split(s);
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
        this.reset();

        // split the given code string into lines, which are each decorated in turn.   
        String[] lines = codeString.split("\n");

        for (int i = 0; i < lines.length; i++) {
            echoToOutputStream(decorate(lines[i]));
            echoToOutputStream("\n");
        }

        return this.resultBuffer.toString();
    }

    /** Splits long lines so they wrap. depends on the above two static variables.
     * @param string the line which is to be split if it is to long
     * @return the initial string if it is short enough. */
    protected String split(final String string) {
        // THIS CODE COULD BE MADE MORE EFFICIENT & CLEANER WITH JUST '<' ' ' SEARCH IN string. 
        // code tuning  (???)
        if (string.length() < this.maxLineLength) {

            return string;
        }

        final String visible = this.PAT.matcher(string).replaceAll("");

        if (visible.length() < this.maxLineLength) {
            return string;
        }

        // Only run if string is too long! 
        final Matcher mat = this.PAT.matcher(string);
        int visibleLenght = 0;
        int groupLength = 0;
        int previousGroupEnd = 0;
        int start = 0;

        do {
            // copy the previous position
            previousGroupEnd = start + groupLength;
            // can a new tag be found ?
            if (mat.find()) {
                start = mat.start();
                groupLength = mat.group().length();
                // the length of the current visible string 
                visibleLenght += start - previousGroupEnd;
                // no more tags found, but (previous) visibleLength < maxLineLength 
            } else {
                start = string.length();
                visibleLenght += start - previousGroupEnd;
                // OK, found the end of string, and it's short enough: don't cut
                if (visibleLenght < this.maxLineLength) {
                    return string;
                }

                // if >= maxLineLength, we'll get out of the do-loop, and same treatment applies.
            }
        } while (visibleLenght < this.maxLineLength);

        // OK, the string read is too long. 
        visibleLenght -= start - previousGroupEnd;

        final String tosplit = string.substring(previousGroupEnd, (this.maxLineLength - visibleLenght) +
            previousGroupEnd);

        // the bit (possibly between tags) which really has to be split
        // We need to split so that visibleLength < maxLineLength, which is done by above tosplit length. 
        int index = tosplit.lastIndexOf(' ');
        // did we find a possible breaking spot? 
        if (index < 1) {

            if (previousGroupEnd == 0) {
                System.err.println("Warning: had to insert hard line break in " + tosplit);
                // define cut at longest allowed line length
                previousGroupEnd = this.maxLineLength;
            }

            index = 0;
        }

        //index+ previousGroupStart is the place where the string should be split!  
        return string.substring(0, index + previousGroupEnd) + '\n' +
            this.split(string.substring(index + previousGroupEnd));
    }

    public int getMaxLineLength() {
        return this.maxLineLength;
    }

    public void setMaxLineLength(final int maxLineLength) {
        this.maxLineLength = maxLineLength;
    }
}
