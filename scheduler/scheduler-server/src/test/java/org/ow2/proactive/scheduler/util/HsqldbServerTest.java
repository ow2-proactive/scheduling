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
package org.ow2.proactive.scheduler.util;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;


/**
 * Tests associated to {@link HsqldbServer}.
 *
 * @author ActiveEon Team
 */
public class HsqldbServerTest {

    @Test
    public void testAddCatalog() {
        HsqldbServer hsqldbServer = new HsqldbServer();

        Properties properties = hsqldbServer.getHsqlProperties().getProperties();

        assertThat(properties).hasSize(0);

        hsqldbServer.addCatalog(Paths.get("a"), "b", "c", "d");

        properties = hsqldbServer.getHsqlProperties().getProperties();

        assertThat(properties).hasSize(2);
        assertThat(properties).containsEntry(HsqldbServer.PROP_HSQLDB_PREFIX_DBNAME + ".0", "b");
        assertThat(properties).containsEntry(HsqldbServer.PROP_HSQLDB_PREFIX_SERVER_DATABASE + ".0",
                                             "file:a;user=c;password=d;");
    }

    @Test
    public void testAddCatalogProperties() throws IOException {
        HsqldbServer hsqldbServer = new HsqldbServer();

        Properties properties = hsqldbServer.getHsqlProperties().getProperties();

        assertThat(properties).hasSize(0);

        String username = "username";
        String password = "password";

        Path hibernateConfiguration = createHibernateConfiguration(ImmutableMap.of(HsqldbServer.PROP_HIBERNATE_CONNECTION_URL,
                                                                                   "jdbc:hsqldb:hsql://localhost:9001/scheduler",
                                                                                   HsqldbServer.PROP_HIBERNATE_CONNECTION_USERNAME,
                                                                                   username,
                                                                                   HsqldbServer.PROP_HIBERNATE_CONNECTION_PASSWORD,
                                                                                   password));

        Path defaultLocation = Paths.get("/default/location");

        hsqldbServer.addCatalog(defaultLocation, hibernateConfiguration);

        properties = hsqldbServer.getHsqlProperties().getProperties();

        assertThat(properties).hasSize(2);
        assertThat(properties).containsEntry(HsqldbServer.PROP_HSQLDB_PREFIX_DBNAME + ".0", "scheduler");
        assertThat(properties).containsEntry(HsqldbServer.PROP_HSQLDB_PREFIX_SERVER_DATABASE + ".0",
                                             "file:" + defaultLocation.resolve("scheduler")
                                                                      .resolve("scheduler")
                                                                      .toString() + ";user=" + username + ";password=" +
                                                                                                     password + ";");

        Path dbPath = Paths.get("build", "RM_DB");

        hibernateConfiguration = createHibernateConfiguration(ImmutableMap.of(HsqldbServer.PROP_HIBERNATE_CONNECTION_URL,
                                                                              "jdbc:hsqldb:hsql://localhost:9001/rm;catalog.path=" +
                                                                                                                          dbPath,
                                                                              HsqldbServer.PROP_HIBERNATE_CONNECTION_USERNAME,
                                                                              username,
                                                                              HsqldbServer.PROP_HIBERNATE_CONNECTION_PASSWORD,
                                                                              password));

        hsqldbServer.addCatalog(defaultLocation, hibernateConfiguration);

        assertThat(properties).hasSize(4);
        assertThat(properties).containsEntry(HsqldbServer.PROP_HSQLDB_PREFIX_DBNAME + ".1", "rm");
        assertThat(properties).containsEntry(HsqldbServer.PROP_HSQLDB_PREFIX_SERVER_DATABASE + ".1",
                                             "file:" + dbPath + ";user=" + username + ";password=" + password + ";");
    }

    @Test(expected = IllegalStateException.class)
    public void testAddCatalogNotAllowedAfterStartup() {
        HsqldbServer hsqldbServer = new HsqldbServer();
        hsqldbServer.startAsync();
        hsqldbServer.addCatalog(Paths.get("a"), "b", "c", "d");
    }

    @Test
    public void testCreateCatalogOptions() {
        HsqldbServer hsqldbServer = new HsqldbServer();

        Path catalogPath = Paths.get("/a/b/c/d/e");
        String catalogOptionLine = "option1=alpha;option2=beta;option3=gamma";
        String catalogUsername = "username";
        String catalogPassword = "password";

        String optionsLine = hsqldbServer.createCatalogOptions(catalogPath,
                                                               catalogOptionLine,
                                                               catalogUsername,
                                                               catalogPassword);

        assertThat(optionsLine).isEqualTo("file:" + catalogPath +
                                          ";user=username;password=password;option1=alpha;option2=beta;option3=gamma");
    }

    @Test
    public void testIdentifyCatalogNameFromConnectionUrl1() {
        HsqldbServer server = new HsqldbServer();

        String catalogName = server.identifyCatalogNameFromConnectionUrl("jdbc:hsqldb:hsql://localhost:9001/scheduler");

        assertThat(catalogName).isEqualTo("scheduler");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIdentifyCatalogNameFromConnectionUrl2() {
        HsqldbServer server = new HsqldbServer();
        server.identifyCatalogNameFromConnectionUrl("");
    }

    @Test
    public void testIdentifyCatalogNameFromConnectionUrl3() {
        HsqldbServer server = new HsqldbServer();

        String catalogName = server.identifyCatalogNameFromConnectionUrl("jdbc:hsqldb:file:build/TEST_SCHEDULER_DB;create=true;hsqldb.tx=mvcc;hsqldb.write_delay=false");

        assertThat(catalogName).isEqualTo("TEST_SCHEDULER_DB");
    }

    @Test
    public void testIdentifyCatalogNameFromConnectionUrl4() {
        HsqldbServer server = new HsqldbServer();

        String catalogName = server.identifyCatalogNameFromConnectionUrl("jdbc:hsqldb:file:test;create=true;hsqldb.tx=mvcc;hsqldb.write_delay=false");

        assertThat(catalogName).isEqualTo("test");
    }

    @Test
    public void testIdentifyCatalogLocationFromConnectionUrl1() {
        HsqldbServer hsqldbServer = new HsqldbServer();

        String catalogLocation = hsqldbServer.identifyCatalogLocationFromConnectionUrl("jdbc:hsqldb:hsql://localhost:9001/rm;catalog.path=build/TEST_RM_DB");

        assertThat(catalogLocation).isEqualTo("build/TEST_RM_DB");
    }

    @Test
    public void testIdentifyCatalogLocationFromConnectionUrl2() {
        HsqldbServer hsqldbServer = new HsqldbServer();

        String catalogLocation = hsqldbServer.identifyCatalogLocationFromConnectionUrl("jdbc:hsqldb:hsql://localhost:9001/rm;catalog.path=build/TEST_RM_DB;");

        assertThat(catalogLocation).isEqualTo("build/TEST_RM_DB");
    }

    @Test
    public void testIdentifyCatalogLocationFromConnectionUrl3() {
        HsqldbServer hsqldbServer = new HsqldbServer();

        String catalogLocation = hsqldbServer.identifyCatalogLocationFromConnectionUrl("jdbc:hsqldb:hsql://localhost:9001/rm;option1=alpha;option2=beta;" +
                                                                                       "option3=gamma;catalog.path=build/TEST_RM_DB;option4=delta;option5=epsilon");

        assertThat(catalogLocation).isEqualTo("build/TEST_RM_DB");
    }

    @Test
    public void testIdentifyCatalogLocationFromConnectionUrl4() {
        HsqldbServer hsqldbServer = new HsqldbServer();

        String catalogLocation = hsqldbServer.identifyCatalogLocationFromConnectionUrl(";==jdbbuil;;d/TEST_;=RM_DB;");

        assertThat(catalogLocation).isNull();
    }

    @Test
    public void testIdentifyCatalogLocationFromConnectionUrl5() {
        HsqldbServer hsqldbServer = new HsqldbServer();

        String catalogLocation = hsqldbServer.identifyCatalogLocationFromConnectionUrl("jdbbuild/TEST_RM_DB;");

        assertThat(catalogLocation).isNull();
    }

    @Test
    public void testIsServerModeRequired() throws Exception {
        Path hibernateConfiguration = createHibernateConfiguration(of(HsqldbServer.PROP_HIBERNATE_CONNECTION_URL,
                                                                      "jdbc:hsqldb:hsql://localhost:9001/scheduler"));

        assertThat(HsqldbServer.isServerModeRequired(hibernateConfiguration)).isTrue();
    }

    @Test
    public void testIsServerModeRequiredMultipleConfigurationFiles() throws Exception {
        Path hibernateConfiguration1 = createHibernateConfiguration(of(HsqldbServer.PROP_HIBERNATE_CONNECTION_URL,
                                                                       "jdbc:mysql://localhost:3306/scheduler"));

        Path hibernateConfiguration2 = createHibernateConfiguration(of(HsqldbServer.PROP_HIBERNATE_CONNECTION_URL,
                                                                       "jdbc:hsqldb:hsql://localhost:9001/scheduler"));

        Path hibernateConfiguration3 = createHibernateConfiguration(ImmutableMap.<String, String> of());

        assertThat(HsqldbServer.isServerModeRequired(hibernateConfiguration1,
                                                     hibernateConfiguration2,
                                                     hibernateConfiguration3)).isTrue();
    }

    @Test
    public void testIsServerModeNotRequiredSinceEmptyFile() throws Exception {
        Path hibernateConfiguration = createHibernateConfiguration(ImmutableMap.<String, String> of());

        assertThat(HsqldbServer.isServerModeRequired(hibernateConfiguration)).isFalse();
    }

    @Test
    public void testIsServerModeNotRequiredSincePropertyNotFound() throws Exception {
        Path hibernateConfiguration = createHibernateConfiguration(ImmutableMap.of("a", "b", "c", "d"));

        assertThat(HsqldbServer.isServerModeRequired(hibernateConfiguration)).isFalse();
    }

    @Test
    public void testIsServerModeNotRequiredMultipleConfigurationFiles() throws Exception {
        Path hibernateConfiguration1 = createHibernateConfiguration(ImmutableMap.<String, String> of());

        Path hibernateConfiguration2 = createHibernateConfiguration(of(HsqldbServer.PROP_HIBERNATE_CONNECTION_URL,
                                                                       "42"));

        Path hibernateConfiguration3 = createHibernateConfiguration(of(HsqldbServer.PROP_HIBERNATE_CONNECTION_URL,
                                                                       "jdbc:mysql://localhost:3306/scheduler"));

        assertThat(HsqldbServer.isServerModeRequired(hibernateConfiguration1,
                                                     hibernateConfiguration2,
                                                     hibernateConfiguration3)).isFalse();
    }

    private Path createHibernateConfiguration(ImmutableMap<String, String> props) throws IOException {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());

        Path config = fs.getPath(UUID.randomUUID().toString() + ".properties");

        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (Map.Entry<String, String> prop : props.entrySet()) {
            builder.add(prop.getKey() + '=' + escapeWindowsPath(prop.getValue()));
        }

        Files.write(config, builder.build(), StandardCharsets.UTF_8);

        return config;
    }

    private String escapeWindowsPath(String path) {
        return path.replace("\\", "\\\\");
    }

    @Test
    public void testIsServerConnectionConfiguredTrue() throws Exception {
        Properties properties = newProperties(of(HsqldbServer.PROP_HIBERNATE_CONNECTION_URL,
                                                 "jdbc:hsqldb:hsql://localhost:9001/scheduler"));

        assertThat(HsqldbServer.isServerConnectionConfigured(properties)).isTrue();
    }

    @Test
    public void testIsServerConnectionConfiguredFalse() throws Exception {
        Properties properties = newProperties(of("unknownProperty", "jdbc:hsqldb:hsql://localhost:9001/scheduler"));

        assertThat(HsqldbServer.isServerConnectionConfigured(properties)).isFalse();
    }

    @Test
    public void testIsServerConnectionConfiguredFalseWithWarning() throws Exception {
        Properties properties = newProperties(of(HsqldbServer.PROP_HIBERNATE_CONNECTION_URL,
                                                 "jdbc:hsqldb:file:${pa.rm.home}/data/db/rm/rm;create=true;hsqldb.tx=mvcc;hsqldb.applog=1"));

        assertThat(HsqldbServer.isServerConnectionConfigured(properties)).isFalse();
    }

    private Properties newProperties(ImmutableMap<String, String> props) {
        Properties properties = new Properties();
        properties.putAll(props);
        return properties;
    }

}
