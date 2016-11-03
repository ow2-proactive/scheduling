/*
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractIdleService;


/**
 * This class encapsulates the logic to create an HSQLDB instance in server mode.
 * <p>
 * The server manages catalogs. Each catalog depicts an isolated database.
 * <p>
 * When HSQLDB is started in server mode, multiple clients can connect to it 
 * at the same time, which is not the case with the file or in-memory mode.
 *
 * @author ActiveEon Team
 */
public class HsqldbServer extends AbstractIdleService {

    private static final Logger logger = Logger.getLogger(HsqldbServer.class);

    /**
     * Hibernate properties.
     */

    protected static final String PROP_HIBERNATE_CONNECTION_URL = "hibernate.connection.url";

    protected static final String PROP_HIBERNATE_CONNECTION_USERNAME = "hibernate.connection.username";

    @SuppressWarnings("squid:S2068")
    protected static final String PROP_HIBERNATE_CONNECTION_PASSWORD = "hibernate.connection.password";

    /*
     * HSQLDB properties.
     */

    protected static final String HSQLDB_DOCUMENTATION_URL = "http://hsqldb.org/doc/guide/dbproperties-chapt.html";

    // custom property used by tests to be able to pass catalog location in Hibernate configuration file
    protected static final String PROP_HSQLDB_PREFIX_CATALOG_PATH = "catalog.path";

    protected static final String PROP_HSQLDB_PREFIX_DBNAME = "server.dbname";

    protected static final String PROP_HSQLDB_PREFIX_SERVER_DATABASE = "server.database";

    // this property is not a common HSQLDB property but a custom one to configure catalog options
    protected static final String PROP_HSQLDB_SERVER_CATALOGS_OPTION_LINE = "server.catalogs.option_line";

    /*
     * Instance variables.
     */

    private int catalogIndex;

    private final String catalogOptionLine;

    private final HsqlProperties hsqlProperties;

    private Server server;

    /**
     * Creates a new instance of HSQLDB server.
     */
    HsqldbServer() {
        catalogOptionLine = "";
        hsqlProperties = new HsqlProperties();
    }

    /**
     * Creates a new instance of HSQLDB server.
     *
     * @param configuration the path to the HSQLDB server properties file.
     *
     * @throws IOException if an error occurs while reading the configuration file.
     */
    public HsqldbServer(Path configuration) throws IOException {

        Properties hsqldbServerProperties = loadProperties(configuration);

        catalogOptionLine = hsqldbServerProperties.getProperty(PROP_HSQLDB_SERVER_CATALOGS_OPTION_LINE);
        hsqldbServerProperties.remove(PROP_HSQLDB_SERVER_CATALOGS_OPTION_LINE);

        hsqlProperties = new HsqlProperties(hsqldbServerProperties);
    }

    public void addCatalog(Path defaultLocation, Path hibernateConfiguration) throws IOException {
        Properties hibernateProperties = loadProperties(hibernateConfiguration);

        String connectionUrl = hibernateProperties.getProperty(PROP_HIBERNATE_CONNECTION_URL);

        String catalogLocation = identifyCatalogLocationFromConnectionUrl(connectionUrl);
        String catalogName = identifyCatalogNameFromConnectionUrl(connectionUrl);
        String catalogPassword = hibernateProperties.getProperty(PROP_HIBERNATE_CONNECTION_PASSWORD);
        String catalogUsername = hibernateProperties.getProperty(PROP_HIBERNATE_CONNECTION_USERNAME);

        Path catalogPath;
        if (catalogLocation == null) {
            // Catalog path references a file.
            // Double resolve is used to get a dedicated folder with the catalog name.
            catalogPath = defaultLocation.resolve(catalogName).resolve(catalogName);
        } else {
            catalogPath = Paths.get(catalogLocation);
        }

        addCatalog(catalogPath, catalogName, catalogUsername, catalogPassword);
    }

    public void addCatalog(Path catalogLocation, String catalogName, String catalogUsername,
            String catalogPassword) {

        if (state() != State.NEW) {
            throw new IllegalStateException("Catalogs configuration must be done before starting the server");
        }

        String catalogIndexAsString = Integer.toString(catalogIndex);

        // See documentation, Table 14.1:
        // http://hsqldb.org/doc/guide/listeners-chapt.html
        hsqlProperties.setProperty(PROP_HSQLDB_PREFIX_SERVER_DATABASE + "." + catalogIndexAsString,
                createCatalogOptions(catalogLocation, catalogOptionLine, catalogUsername, catalogPassword));
        hsqlProperties.setProperty(PROP_HSQLDB_PREFIX_DBNAME + "." + catalogIndexAsString, catalogName);

        catalogIndex++;
    }

    @VisibleForTesting
    String createCatalogOptions(Path catalogPath, String catalogOptionLine, String catalogUsername,
            String catalogPassword) {
        return "file:" + catalogPath.toString() + ";user=" + catalogUsername + ";password=" +
            catalogPassword + ";" + catalogOptionLine;
    }

    // See HSQLDB documentation, table 13.4 (Server Database URL) for input examples
    // http://hsqldb.org/doc/guide/dbproperties-chapt.html#dpc_db_props_url
    @VisibleForTesting
    String identifyCatalogNameFromConnectionUrl(String connectionUrl) {
        String[] chunks = connectionUrl.split(":");

        if (chunks.length != 4 && chunks.length != 5) {
            throw new IllegalArgumentException("Invalid connection URL: " + connectionUrl +
                ". Please look at the HSQLDB documentation: " + HSQLDB_DOCUMENTATION_URL);
        }

        // discard connection options
        chunks = chunks[chunks.length - 1].split(";");
        // split to identify catalog name
        chunks = chunks[0].split("/");

        return chunks[chunks.length - 1];
    }

    @VisibleForTesting
    String identifyCatalogLocationFromConnectionUrl(String connectionUrl) {
        String[] chunks = connectionUrl.split(";");

        for (String chunk : chunks) {
            String[] split = chunk.split("=");

            if (split.length == 1) {
                continue;
            }

            String key = split[0];
            String value = split[1];

            if (PROP_HSQLDB_PREFIX_CATALOG_PATH.equals(key)) {
                return value;
            }
        }

        return null;
    }

    @VisibleForTesting
    HsqlProperties getHsqlProperties() {
        return hsqlProperties;
    }

    @Override
    protected void startUp() throws Exception {
        server = new Server();
        server.setProperties(hsqlProperties);
        server.setErrWriter(null);
        server.setLogWriter(null);
        server.start();
    }

    @Override
    protected void shutDown() throws Exception {
        server.stop();
    }

    public static boolean isServerModeRequired(Path hibernateConfiguration, Path... others)
            throws IOException {

        List<Path> hibernateConfigurations = ImmutableList.<Path> builder().add(hibernateConfiguration)
                .add(others).build();

        boolean result = false;

        for (Path config : hibernateConfigurations) {
            Properties hibernateProperties = loadProperties(config);
            result |= isServerConnectionConfigured(hibernateProperties);
        }

        return result;
    }

    /**
     * Check if the server connection for HSQLDB is set for the given set of properties.
     *
     * @param hibernateProperties the properties to look at for checking configuration.
     * @return a boolean that says if starting HSQLDB in server mode is required.
     */
    @VisibleForTesting
    static boolean isServerConnectionConfigured(Properties hibernateProperties) {
        String dbConnectionUrl = hibernateProperties.getProperty(PROP_HIBERNATE_CONNECTION_URL);

        if (dbConnectionUrl == null) {
            return false;
        } else if (dbConnectionUrl.startsWith("jdbc:hsqldb:hsql")) {
            return true;
        } else if (dbConnectionUrl.startsWith("jdbc:hsqldb:")) {
            String message = "Non server database URL detected. HSQLDB must be configured in server mode to " +
                "work smoothly with ProActive Workflows and Scheduling. Please look at HSQLDB documentation: " +
                HSQLDB_DOCUMENTATION_URL;
            logger.warn(message);
            return false;
        } else {
            return false;
        }
    }

    private static Properties loadProperties(Path configuration) throws IOException {
        Properties dbProperties = new Properties();

        try (InputStream stream = Files.newInputStream(configuration)) {
            dbProperties.load(stream);
        }

        return dbProperties;
    }

}
