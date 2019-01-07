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
package org.ow2.proactive.scheduler;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ow2.proactive.scheduler.examples.connectionpooling.DBConnectionDetails;


/**
 * @author ActiveEon Team
 * @since 03/01/2019
 */
public class DBConnectionDetailsTest {

    @Test
    public void testEqualsAndHashCodeOK1() {
        DBConnectionDetails dbConnectionDetails1 = new DBConnectionDetails.Builder().jdbcUrl("jdbc:mysql://localhost:123/mydb")
                                                                                    .username("toto")
                                                                                    .addDataSourceProperty("prop",
                                                                                                           "value")
                                                                                    .build();
        DBConnectionDetails dbConnectionDetails2 = new DBConnectionDetails.Builder().jdbcUrl("jdbc:mysql://localhost:123/mydb")
                                                                                    .username("toto")
                                                                                    .addDataSourceProperty("prop",
                                                                                                           "value")
                                                                                    .build();
        assertTrue(dbConnectionDetails1.equals(dbConnectionDetails1));
        assertEquals(dbConnectionDetails1.hashCode(), dbConnectionDetails2.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeOK2() {
        DBConnectionDetails dbConnectionDetails1 = new DBConnectionDetails.Builder().jdbcUrl("jdbc:mysql://localhost:123/mydb")
                                                                                    .username("toto")
                                                                                    .addDataSourceProperty("prop",
                                                                                                           "value")
                                                                                    .build();
        DBConnectionDetails dbConnectionDetails2 = new DBConnectionDetails.Builder().jdbcUrl("jdbc:mysql://localhost:123/mydb")
                                                                                    .username("toto")
                                                                                    .password("password2")
                                                                                    .addDataSourceProperty("prop",
                                                                                                           "value")
                                                                                    .build();
        assertTrue(dbConnectionDetails2.equals(dbConnectionDetails1));
        assertEquals(dbConnectionDetails1.hashCode(), dbConnectionDetails2.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeOK3() {
        DBConnectionDetails dbConnectionDetails1 = new DBConnectionDetails.Builder().jdbcUrl("jdbc:mysql://localhost:123/mydb")
                                                                                    .username("toto")
                                                                                    .password("password1")
                                                                                    .addDataSourceProperty("prop",
                                                                                                           "value")
                                                                                    .build();
        DBConnectionDetails dbConnectionDetails2 = new DBConnectionDetails.Builder().jdbcUrl("jdbc:mysql://localhost:123/mydb")
                                                                                    .username("toto")
                                                                                    .password("password2")
                                                                                    .addDataSourceProperty("prop",
                                                                                                           "value")
                                                                                    .build();
        assertTrue(dbConnectionDetails2.equals(dbConnectionDetails1));
        assertEquals(dbConnectionDetails1.hashCode(), dbConnectionDetails2.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeKO1() {
        DBConnectionDetails dbConnectionDetails1 = new DBConnectionDetails.Builder().jdbcUrl("jdbc:mysql://localhost:123/mydb1")
                                                                                    .username("toto")
                                                                                    .addDataSourceProperty("prop",
                                                                                                           "value")
                                                                                    .build();
        DBConnectionDetails dbConnectionDetails2 = new DBConnectionDetails.Builder().jdbcUrl("jdbc:mysql://localhost:123/mydb2")
                                                                                    .username("toto")
                                                                                    .password("password")
                                                                                    .addDataSourceProperty("prop",
                                                                                                           "value")
                                                                                    .build();
        assertFalse(dbConnectionDetails2.equals(dbConnectionDetails1));
        assertNotEquals(dbConnectionDetails1.hashCode(), dbConnectionDetails2.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeKO2() {
        DBConnectionDetails dbConnectionDetails1 = new DBConnectionDetails.Builder().jdbcUrl("jdbc:mysql://localhost:123/mydb")
                                                                                    .username("toto1")
                                                                                    .addDataSourceProperty("prop",
                                                                                                           "value")
                                                                                    .build();
        DBConnectionDetails dbConnectionDetails2 = new DBConnectionDetails.Builder().jdbcUrl("jdbc:mysql://localhost:123/mydb")
                                                                                    .username("toto2")
                                                                                    .addDataSourceProperty("prop",
                                                                                                           "value")
                                                                                    .build();
        assertFalse(dbConnectionDetails2.equals(dbConnectionDetails1));
        assertNotEquals(dbConnectionDetails1.hashCode(), dbConnectionDetails2.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeKO3() {
        DBConnectionDetails dbConnectionDetails1 = new DBConnectionDetails.Builder().jdbcUrl("jdbc:mysql://localhost:123/mydb")
                                                                                    .username("toto")
                                                                                    .addDataSourceProperty("prop1",
                                                                                                           "value")
                                                                                    .build();
        DBConnectionDetails dbConnectionDetails2 = new DBConnectionDetails.Builder().jdbcUrl("jdbc:mysql://localhost:123/mydb")
                                                                                    .username("toto")
                                                                                    .addDataSourceProperty("prop2",
                                                                                                           "value")
                                                                                    .build();
        assertFalse(dbConnectionDetails2.equals(dbConnectionDetails1));
        assertNotEquals(dbConnectionDetails1.hashCode(), dbConnectionDetails2.hashCode());
    }

    @Test
    public void testHiddenPassword() {
        DBConnectionDetails dbConnectionDetails1 = new DBConnectionDetails.Builder().jdbcUrl("jdbc:mysql://localhost:123/mydb")
                                                                                    .username("toto")
                                                                                    .password("password")
                                                                                    .addDataSourceProperty("prop",
                                                                                                           "value")
                                                                                    .build();
        assertThat(dbConnectionDetails1.toString(), not(containsString("password")));
        assertThat(dbConnectionDetails1.toString(), containsString("toto"));
        assertThat(dbConnectionDetails1.toString(), containsString("l://localhost:"));

    }
}
