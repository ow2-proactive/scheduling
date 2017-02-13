/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scripting.helper.forkenvironment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;


public class ForkEnvironmentScriptResultExtractorTest {

    /**
     * Test that null will return an empty string array.
     */
    @Test
    public void testPassingNullWillReturnEmptyArray() {
        ForkEnvironmentScriptResultExtractor forkEnvironmentScriptResultExtractor = new ForkEnvironmentScriptResultExtractor();
        String[] emptyArray = forkEnvironmentScriptResultExtractor.getJavaPrefixCommand(null);

        assertThat(emptyArray.length, is(0));
    }

    /**
     * Tests that an empty has map, or one which does not contain the java prefix command key, will
     * return and empty string array.
     */
    @Test
    public void testPassingEmptyMapWillReturnEmptyStringArray() {
        ForkEnvironmentScriptResultExtractor forkEnvironmentScriptResultExtractor = new ForkEnvironmentScriptResultExtractor();
        String[] emptyArray = forkEnvironmentScriptResultExtractor.getJavaPrefixCommand(new HashMap<String, Object>());

        assertThat(emptyArray.length, is(0));
    }

    /**
     * Tests that a prefix command which is not an char sequence will return an empty list.
     */
    @Test
    public void testPassingMapWithNoCharSequence() {
        ForkEnvironmentScriptResultExtractor forkEnvironmentScriptResultExtractor = new ForkEnvironmentScriptResultExtractor();
        Map<String, Object> intPrefixCommand = new HashMap<>();

        // Put an int instead of a CharSequence
        intPrefixCommand.put(ForkEnvironmentScriptResultExtractor.PRE_JAVA_CMD_KEY, 10);

        // Extract command
        String[] emptyArray = forkEnvironmentScriptResultExtractor.getJavaPrefixCommand(intPrefixCommand);

        // Check that it returns an empty list
        assertThat(emptyArray.length, is(0));
    }

    /**
     * Test that the class retrieves the class, which is saved inside a private String.
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    public void testCorrectStringIsExtracted() throws NoSuchFieldException, IllegalAccessException {
        String valueOfKey = "correct";

        String[] prefixCommand = insertAndRetrieveKeyFromMap(valueOfKey);

        assertThat(prefixCommand[0], is(valueOfKey));
    }

    /**
     * Test that the method splits the command string by spaces.
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Test
    public void testThatPrefixCommandIsSplitBySpace() throws NoSuchFieldException, IllegalAccessException {
        String[] expectedOutput = { "docker", "run", "-v", "/tmp/:/tmp/", "tobwiens/proactive" };
        String valueOfKey = "docker run -v /tmp/:/tmp/ tobwiens/proactive";

        String[] prefixCommand = insertAndRetrieveKeyFromMap(valueOfKey);

        assertThat(prefixCommand, is(expectedOutput));
    }

    /**
     * Inserts two keys into a map, one is not important and one is looked for later.
     *
     * @param valueOfKey The value which is given to the key searched for.
     * @return A String array, which is represents the valueOfKey retrieved from the Map and split
     * by whitespaces.
     */
    private String[] insertAndRetrieveKeyFromMap(String valueOfKey) {
        ForkEnvironmentScriptResultExtractor forkEnvironmentScriptResultExtractor = new ForkEnvironmentScriptResultExtractor();

        Map<String, Object> filledHashMap = new HashMap<>();
        filledHashMap.put("Something1", "Something1");
        filledHashMap.put(ForkEnvironmentScriptResultExtractor.PRE_JAVA_CMD_KEY, valueOfKey);

        return forkEnvironmentScriptResultExtractor.getJavaPrefixCommand(filledHashMap);
    }
}
