package org.objectweb.proactive.extra.scheduler.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


/**
 * Initialize a database manager since a property file. The file must contain
 * "driver", "protocol", "db_path" (database path), "db_name" (database name),
 * "user" and "password" properties.
 *
 * @author FRADJ Johann
 */
public class DatabaseManager {

    /**
     * the property file name. If the name start with a "/", so the file will be
     * search relative to the classpath, otherwise it will be search relative to
     * the directory containing the ConfigConnection.class
     */
    public static final String PROPERTY_FILE_NAME = "db.cfg";
    private static DatabaseManager instance = null;
    private String driver = null;
    private String protocol = null;
    private String databasePath = null;
    private String databaseName = null;
    private String user = null;
    private String password = null;

    /**
     * The default constructor.
     *
     * @throws IOException
     */
    private DatabaseManager() throws IOException {
        Properties props = new Properties();
        URL urlPropertyFile = DatabaseManager.class.getResource(PROPERTY_FILE_NAME);
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(urlPropertyFile.openStream());
            props.load(bis);
            driver = props.getProperty("driver");
            protocol = props.getProperty("protocol");
            databasePath = props.getProperty("db_path");
            databaseName = props.getProperty("db_name");
            user = props.getProperty("user");
            password = props.getProperty("password");
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * To load the database driver, connect to the database and obtain a
     * connection.
     *
     * @param create true if the database must be created.
     * @return a connection to the database
     */
    public Connection connect(boolean create) throws SQLException {
        String url = protocol + databasePath + databaseName +
            ((create) ? ";create=true" : ";");

        System.out.println("[SCHEDULER-DATABASE] url=" + url);
        try {
            Class.forName(driver).newInstance();
            return DriverManager.getConnection(url, user, password);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * To disconnect the database and shutdown it.
     *
     * @return true only if the database shutdown successfully
     */
    public boolean disconnect() {
        try {
            DriverManager.getConnection(protocol + ";shutdown=true");
        } catch (SQLException e) {
            return true;
        }
        return false;
    }

    /**
     * Return the current instance, if the instance is null then this method
     * will create a new instance before returning it.
     *
     * @return the instance
     */
    public static DatabaseManager getInstance() {
        try {
            instance = new DatabaseManager();
            return instance;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
