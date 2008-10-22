/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.hsql;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Shell for JDBC transactions of all types. This provides a base class to the
 * database transactional classes.
 *
 * @author Amr Alam, Refractions Research
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/hsql/src/main/java/org/geotools/data/hsql/HsqlConnection.java $
 */
public class HsqlConnection implements javax.sql.DataSource {
    //private static Category _log = Category.getInstance(MysqlConnection.class.getName());

    /** The Mysql-specific JDBC driver class using mm.mysql */
    private static final String HSQL_DRIVER_CLASS = "org.hsqldb.jdbcDriver";

    /** The Mysql-specific JDBC driver path. */
    private static final String HSQL_DRIVER_PATH = "jdbc:hsql";

    /** the computer where the database to connect to resides */
    private String host;

    /** The port to connect on */
    private String port;

    /** The name of the database to connect to */
    private String dbName;

    /** The name of the user to log in to the database */
    private String user = null;

    /** The password of the user to log in to the database */
    private String password = null;

    /**
     * Constructor with all internal database driver classes, driver paths and
     * database types.
     *
     * @param host The driver class; should be passed from the
     *        database-specific subclass.
     * @param port The driver path; should be passed from the database-specific
     *        subclass.
     * @param dbName The database type; should be passed from the
     *        database-specific subclass.
     */
    public HsqlConnection(String host, String port, String dbName) {
        this.host = host;
        this.port = port;
        this.dbName = dbName;
    }

    /**
     * Sets the user and password strings of the login to be used when
     * connecting to the Mysql database.
     *
     * @param user The string of the user to connect to the database with.
     * @param password The string of the password of user.
     */
    public void setLogin(String user, String password) {
        this.user = user;
        this.password = password;
    }

    /**
     * An accessor function to get the user to log in to the db.
     *
     * @return the user.
     */
    public String getLoginUser() {
        return user;
    }

    /**
     * An accessor function to get the password to log in to the db.
     *
     * @return a string of the password.
     */
    public String getLoginPassword() {
        return password;
    }

    /**
     * Retrieves a connection to the Mysql database, using the current user and
     * password;
     *
     * @return An open SQL connection with the database.
     *
     * @throws SQLException if there are any database problems.
     */
    public Connection getConnection() throws SQLException {
        return getConnection(user, password);
    }

    /**
     * Retrieves a connection to the Mysql database, specifying a user and
     * password.
     *
     * @param user The string of the user to connect to the database with.
     * @param password The string of the corresponding password of user.
     *
     * @return An open SQL connection with the database.
     *
     * @throws SQLException if there are any database problems.
     */
    public Connection getConnection(String user, String password)
        throws SQLException {
        // creates the string to connect with
        String connectionPath = HSQL_DRIVER_PATH + "://" + host + ":" + port
            + "/" + dbName;
        Connection dbConnection = null;

        // Instantiate the driver classes
        try {
            Class.forName(HSQL_DRIVER_CLASS);
            dbConnection = DriverManager.getConnection(connectionPath, user,
                    password);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Hsql driver was not found.");
        }

        return dbConnection;
    }

    /**
     * An accessor function to get the length of timeout.
     *
     * @return the time out.
     *
     * @task TODO: implement this.
     */
    public int getLoginTimeout() {
        return 10;
    }

    /**
     * A setter function to get the length of timeout.
     *
     * @param seconds the length of the time out.
     *
     * @task TODO: implement this.
     */
    public void setLoginTimeout(int seconds) {
    }

    /**
     * An accessor function to get the log writer.
     *
     * @return a writer
     *
     * @task TODO: implement this.
     */
    public PrintWriter getLogWriter() {
        return new PrintWriter(System.out);
    }

    /**
     * An setter method to set the log writer.
     *
     * @param out the writer to use for logging.
     *
     * @task TODO: implement this.
     */
    public void setLogWriter(PrintWriter out) {
    }
}
