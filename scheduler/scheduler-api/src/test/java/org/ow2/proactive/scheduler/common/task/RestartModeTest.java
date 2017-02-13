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
package org.ow2.proactive.scheduler.common.task;

import static com.google.common.truth.Truth.assertThat;
import static org.ow2.proactive.scheduler.common.task.RestartMode.ANYWHERE;
import static org.ow2.proactive.scheduler.common.task.RestartMode.ELSEWHERE;
import static org.ow2.proactive.scheduler.common.task.RestartMode.getMode;

import org.junit.Test;


/**
 * @author ActiveEon Team
 */
public class RestartModeTest {

    @Test
    public void testConstructor() {
        RestartMode restartMode = new RestartMode(7, "restartMode");
        assertThat(restartMode.getIndex()).isEqualTo(7);
        assertThat(restartMode.getDescription()).isEqualTo("restartMode");
    }

    @Test
    public void testGetModeUsingDescriptionAnywhere1() {
        assertThat(getMode("Anywhere")).isEqualTo(ANYWHERE);
    }

    @Test
    public void testGetModeUsingDescriptionAnywhere2() {
        assertThat(getMode("AnYwHeRe")).isEqualTo(ANYWHERE);
    }

    @Test
    public void testGetModeUsingDescriptionElsewhere1() {
        assertThat(getMode("Elsewhere")).isEqualTo(ELSEWHERE);
    }

    @Test
    public void testGetModeUsingDescriptionElsewhere2() {
        assertThat(getMode("ElSeWhErE")).isEqualTo(ELSEWHERE);
    }

    @Test
    public void testGetModeUsingDescriptionAnyInput1() {
        assertThat(getMode("any")).isEqualTo(ANYWHERE);
    }

    @Test
    public void testGetModeUsingDescriptionAnyInput2() {
        assertThat(getMode("")).isEqualTo(ANYWHERE);
    }

    @Test
    public void testGetModeUsingIdAnywhere1() {
        assertThat(getMode(1)).isEqualTo(ANYWHERE);
    }

    @Test
    public void testGetModeUsingIdElsewhere1() {
        assertThat(getMode(2)).isEqualTo(ELSEWHERE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetModeUsingInvalidId1() {
        getMode(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetModeUsingInvalidId2() {
        getMode(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetModeUsingInvalidId3() {
        getMode(3);
    }

    @Test
    public void testGetDescriptionForAnywhereOption() {
        assertThat(ANYWHERE.getDescription()).isEqualTo("Anywhere");
    }

    @Test
    public void testGetDescriptionForElsewhereOption() {
        assertThat(ELSEWHERE.getDescription()).isEqualTo("Elsewhere");
    }

    @Test
    public void testGetIndexForAnywhereOption() {
        assertThat(ANYWHERE.getIndex()).isEqualTo(1);
    }

    @Test
    public void testGetIndexForElsewhereOption() {
        assertThat(ELSEWHERE.getIndex()).isEqualTo(2);
    }

    @Test
    public void testToStringAnywhereOption() {
        assertThat(ANYWHERE.toString()).isEqualTo(ANYWHERE.getDescription());
    }

    @Test
    public void testToStringElsewhereOption() {
        assertThat(ELSEWHERE.toString()).isEqualTo(ELSEWHERE.getDescription());
    }

    @Test
    public void testHashCodeAnywhereOption() {
        assertThat(ANYWHERE.hashCode()).isEqualTo(ANYWHERE.getIndex());
    }

    @Test
    public void testHashCodeElsewhereOption() {
        assertThat(ELSEWHERE.hashCode()).isEqualTo(ELSEWHERE.getIndex());
    }

    @Test
    public void testEquality1() {
        assertThat(ANYWHERE.equals(ANYWHERE)).isTrue();
    }

    @Test
    public void testEquality2() throws Exception {
        assertThat(ANYWHERE.equals(ELSEWHERE)).isFalse();
    }

    @Test
    public void testEquality3() throws Exception {
        assertThat(ANYWHERE.equals(ELSEWHERE)).isFalse();
    }

    @Test
    public void testEquality4() throws Exception {
        assertThat(ANYWHERE.equals(null)).isFalse();
    }

}
