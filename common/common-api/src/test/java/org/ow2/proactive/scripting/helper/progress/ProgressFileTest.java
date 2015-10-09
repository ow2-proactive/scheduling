/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scripting.helper.progress;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProgressFileTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path file;

    @Before
    public void setup() throws IOException {
        file = folder.newFile().toPath();
    }

    @Test
    public void testSetProgress0() {
        assertThat(ProgressFile.setProgress(file, 0), is(true));
    }

    @Test
    public void testSetProgress42() {
        assertThat(ProgressFile.setProgress(file, 42), is(true));
    }

    @Test
    public void testSetProgress100() {
        assertThat(ProgressFile.setProgress(file, 100), is(true));
    }

    @Test
    public void testSetProgress142() {
        assertThat(ProgressFile.setProgress(file, 142), is(false));
    }

    @Test
    public void testSetProgressMinus1() {
        assertThat(ProgressFile.setProgress(file, -1), is(false));
    }

    @Test
    public void testSetProgressMinus42() {
        assertThat(ProgressFile.setProgress(file, -42), is(false));
    }

    @Test
    public void testGetProgress0() {
        testGetProgress(0);
    }

    @Test
    public void testGetProgress42() {
        testGetProgress(42);
    }

    @Test
    public void testGetProgress100() {
        testGetProgress(100);
    }

    @Test
    public void testGetProgress142() {
        testGetProgress(-1, "142");
    }

    @Test
    public void testGetProgressMinus1() {
        testGetProgress(-1);
    }

    @Test
    public void testGetProgressMinus42() {
        testGetProgress(-1, "-42");
    }

    @Test
    public void testGetProgressNotNumeric() {
        testGetProgress(-1, "abc");
    }

    private void testGetProgress(int value) {
        testGetProgress(value, Integer.toString(value));
    }

    private void testGetProgress(int expectedValue, String value) {
        // use ProgressFile#setProgress(Path, String) to allow to set invalid values
        boolean setProgressResult = ProgressFile.setProgress(file, value);

        assertThat("Set progress has failed", setProgressResult, is(true));
        assertThat(ProgressFile.getProgress(file), is(expectedValue));
    }

}