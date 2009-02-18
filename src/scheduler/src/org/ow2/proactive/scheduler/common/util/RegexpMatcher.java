package org.ow2.proactive.scheduler.common.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * RegexpMatcher should help you when looking for patterns in a string.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
@PublicAPI
public class RegexpMatcher implements Serializable {

    private static final long serialVersionUID = 3248173020881015123L;
    private Pattern pattern = null;
    private Matcher matcher = null;
    private ArrayList<String> matches;
    private int cursor;

    /**
     * Create a new instance of RegexpMatcher.
     *
     * @param pattern the pattern to find in the string. It is no need to surround your string with the .* characters.
     */
    public RegexpMatcher(String pattern) {
        this.pattern = Pattern.compile("(" + pattern + ")");
    }

    /**
     * Matches the regular expression with the given string.
     *
     * @param toMatch the String where to find the regular expression.
     */
    public void matches(String toMatch) {
        matcher = pattern.matcher(toMatch);
        matches = new ArrayList<String>();
        while (matcher.find()) {
            matches.add(matcher.group(1));
        }
        cursor = 0;
    }

    /**
     * Once matches, returns the next string instance matches by the pattern.
     *
     * @return the next string instance matches by the pattern.
     */
    public String next() {
        return matches.get(cursor++);
    }

    /**
     * Tell if an other matches is existing.
     *
     * @return true if there is an other matches, false otherwise.
     */
    public boolean hasNext() {
        return matches.size() > cursor;
    }

    /**
     * Return an array that contains all the string matched by the given pattern in the given string.
     *
     * @param pattern the pattern to find in the string. It is no need to surround your string with the .* characters.
     * @param toMatch the String where to find the regular expression.
     * @return an array that contains all the string matched by the given pattern in the given string.
     * 			return an empty array if nothing found.
     */
    public static String[] matches(String pattern, String toMatch) {
        RegexpMatcher matcher = new RegexpMatcher(pattern);
        matcher.matches(toMatch);
        return matcher.matches.toArray(new String[] {});
    }

}
