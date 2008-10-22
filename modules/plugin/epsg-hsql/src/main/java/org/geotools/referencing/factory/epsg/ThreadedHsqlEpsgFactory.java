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
package org.geotools.referencing.factory.epsg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;

import org.geotools.util.Version;
import org.geotools.util.logging.Logging;
import org.geotools.factory.Hints;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Loggings;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.referencing.factory.AbstractAuthorityFactory;

import org.hsqldb.jdbc.jdbcDataSource;


/**
 * Connection to the EPSG database in HSQL database engine format using JDBC. The EPSG
 * database can be downloaded from <A HREF="http://www.epsg.org">http://www.epsg.org</A>.
 * The SQL scripts (modified for the HSQL syntax as <A HREF="doc-files/HSQL.html">explained
 * here</A>) are bundled into this plugin. The database version is given in the
 * {@linkplain org.opengis.metadata.citation.Citation#getEdition edition attribute}
 * of the {@linkplain org.opengis.referencing.AuthorityFactory#getAuthority authority}.
 * The HSQL database is read only.
 * <P>
 * <H3>Implementation note</H3>
 * The SQL scripts are executed the first time a connection is required. The database
 * is then created as cached tables ({@code HSQL.properties} and {@code HSQL.data} files)
 * in a temporary directory. Future connections to the EPSG database while reuse the cached
 * tables, if available. Otherwise, the scripts will be executed again in order to recreate
 * them.
 * <p>
 * If the EPSG database should be created in a different directory (or already exists in that
 * directory), it may be specified as a {@linkplain System#getProperty(String) system property}
 * nammed {@value #DIRECTORY_KEY}.
 *
 * @since 2.4
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/plugin/epsg-hsql/src/main/java/org/geotools/referencing/factory/epsg/ThreadedHsqlEpsgFactory.java $
 * @version $Id: ThreadedHsqlEpsgFactory.java 31445 2008-09-07 18:14:23Z desruisseaux $
 * @author Martin Desruisseaux
 * @author Didier Richard
 */
public class ThreadedHsqlEpsgFactory extends ThreadedEpsgFactory {
    /**
     * Current version of EPSG-HSQL plugin. This is usually the same version number than the
     * one in the EPSG database bundled in this plugin. However this field may contains
     * additional minor version number if there is some changes related to the EPSG-HSQL
     * plugin rather then the EPSG database itself (for example additional database index).
     */
    public static final Version VERSION = new Version("6.12.0");

    /**
     * The key for fetching the database directory from {@linkplain System#getProperty(String)
     * system properties}.
     */
    public static final String DIRECTORY_KEY = "EPSG-HSQL.directory";

    /**
     * The name of the SQL file to read in order to create the cached database.
     */
    private static final String SQL_FILE = "EPSG.sql";

    /**
     * The database name.
     */
    public static final String DATABASE_NAME = "EPSG";

    /**
     * The prefix to put in front of URL to the database.
     */
    private static final String PREFIX = "jdbc:hsqldb:file:";

    /**
     * The logger name.
     */
    private static final String LOGGER = "org.geotools.referencing.factory.epsg";

    /**
     * Creates a new instance of this factory. If the {@value #DIRECTORY_KEY}
     * {@linkplain System#getProperty(String) system property} is defined and contains
     * the name of a directory with a valid {@linkplain File#getParent parent}, then the
     * {@value #DATABASE_NAME} database will be saved in that directory. Otherwise, a
     * temporary directory will be used.
     */
    public ThreadedHsqlEpsgFactory() {
        this(null);
    }

    /**
     * Creates a new instance of this data source using the specified hints. The priority
     * is set to a lower value than the {@linkplain FactoryOnAccess}'s one in order to give
     * precedence to the Access-backed database, if presents. Priorities are set that way
     * because:
     * <ul>
     *   <li>The MS-Access format is the primary EPSG database format.</li>
     *   <li>If a user downloads the MS-Access database himself, he probably wants to use it.</li>
     * </ul>
     */
    public ThreadedHsqlEpsgFactory(final Hints hints) {
        super(hints, PRIORITY + 1);
    }

    /**
     * Returns the default directory for the EPSG database. If the {@value #DIRECTORY_KEY}
     * {@linkplain System#getProperty(String) system property} is defined and contains the
     * name of a directory with a valid {@linkplain File#getParent parent}, then the
     * {@value #DATABASE_NAME} database will be saved in that directory. Otherwise,
     * a temporary directory will be used.
     */
    private static File getDirectory() {
        try {
            final String property = System.getProperty(DIRECTORY_KEY);
            if (property != null) {
                final File directory = new File(property);
                /*
                 * Creates the directory if needed (mkdir), but NOT the parent directories (mkdirs)
                 * because a missing parent directory may be a symptom of an installation problem.
                 * For example if 'directory' is a subdirectory in the temporary directory (~/tmp/),
                 * this temporary directory should already exists. If it doesn't, an administrator
                 * should probably looks at this problem.
                 */
                if (directory.isDirectory() || directory.mkdir()) {
                    return directory;
                }
            }
        } catch (SecurityException e) {
            /*
             * Can't fetch the base directory from system properties.
             * Fallback on the default temporary directory.
             */
        }
        return getTemporaryDirectory();
    }

    /**
     * Returns the directory to uses in the temporary directory folder.
     */
    private static File getTemporaryDirectory() {
        File directory = new File(System.getProperty("java.io.tmpdir", "."), "Geotools");
        if (directory.isDirectory() || directory.mkdir()) {
            directory = new File(directory, "Databases/HSQL");
            if (directory.isDirectory() || directory.mkdirs()) {
                return directory;
            }
        }
        return null;
    }

    /**
     * Extract the directory from the specified data source, or {@code null} if this
     * information is not available.
     */
    private static File getDirectory(final DataSource source) {
        if (source instanceof jdbcDataSource) {
            String path = ((jdbcDataSource) source).getDatabase();
            if (path!=null && PREFIX.regionMatches(true, 0, path, 0, PREFIX.length())) {
                path = path.substring(PREFIX.length());
                return new File(path).getParentFile();
            }
        }
        return null;
    }

    /**
     * Returns a data source for the HSQL database.
     */
    protected DataSource createDataSource() throws SQLException {
        DataSource candidate = super.createDataSource();
        if (candidate instanceof jdbcDataSource) {
            return candidate;
        }
        final jdbcDataSource source = new jdbcDataSource();
        File directory = getDirectory();
        if (directory != null) {
            /*
             * Constructs the full path to the HSQL database. Note: we do not use
             * File.toURI() because HSQL doesn't seem to expect an encoded URL
             * (e.g. "%20" instead of spaces).
             */
            final StringBuilder url = new StringBuilder(PREFIX);
            final String path = directory.getAbsolutePath().replace(File.separatorChar, '/');
            if (path.length()==0 || path.charAt(0)!='/') {
                url.append('/');
            }
            url.append(path);
            if (url.charAt(url.length()-1) != '/') {
                url.append('/');
            }
            url.append(DATABASE_NAME);
            source.setDatabase(url.toString());
            assert directory.equals(getDirectory(source)) : url;
        }
        /*
         * If the temporary directory do not exists or can't be created, lets the 'database'
         * attribute unset. If the user do not set it explicitly (through JNDI or by overrding
         * this method), an exception will be thrown when 'createBackingStore()' will be invoked.
         */
        source.setUser("SA"); // System administrator. No password.
        return source;
    }

    /**
     * Returns {@code true} if the database contains data. This method returns {@code false}
     * if an empty EPSG database has been automatically created by HSQL and not yet populated.
     */
    private static boolean dataExists(final Connection connection) throws SQLException {
        final ResultSet tables = connection.getMetaData().getTables(
                null, null, "EPSG_%", new String[] {"TABLE"});
        final boolean exists = tables.next();
        tables.close();
        return exists;
    }

    /**
     * Compares the {@code "epsg.version"} property in the specified file with the expected
     * {@link #VERSION}. If the version found in the property file is equals or higher than
     * the expected one, then this method do nothing. Otherwise or if no version information
     * is found in the property file, then this method clean the temporary directory
     * containing the cached database.
     */
    private static void deleteIfOutdated(final File directory, final File propertyFile) {
        if (directory == null || !directory.equals(getTemporaryDirectory())) {
            /*
             * Never touch to the directory if it is not in the temporary directory.
             * It may be a user file!
             */
            return;
        }
        if (propertyFile.isFile()) try {
            final InputStream propertyIn = new FileInputStream(propertyFile);
            final Properties properties  = new Properties();
            properties.load(propertyIn);
            propertyIn.close();
            final String version = properties.getProperty("epsg.version");
            if (version != null) {
                if (new Version(version).compareTo(VERSION) >= 0) {
                    return;
                }
            }
        } catch (IOException exception) {
            /*
             * Failure to read the property file. This is just a warning, not an error, because
             * we will attempt to rebuild the whole database. Note: "createBackingStore" is the
             * public method that invoked this method, so we use it for the logging message.
             */
            Logging.unexpectedException(LOGGER,
                    ThreadedHsqlEpsgFactory.class, "createBackingStore", exception);
        }
        delete(directory);
    }

    /**
     * Deletes the specified directory and all sub-directories. Used for
     * cleaning the temporary directory containing the cached database only.
     */
    private static void delete(final File directory) {
        if (directory != null) {
            final File[] files = directory.listFiles();
            if (files != null) {
                for (int i=0; i<files.length; i++) {
                    delete(files[i]);
                }
            }
            directory.delete();
        }
    }

    /**
     * Returns the backing-store factory for HSQL syntax. If the cached tables are not available,
     * they will be created now from the SQL scripts bundled in this plugin.
     *
     * @param  hints A map of hints, including the low-level factories to use for CRS creation.
     * @return The EPSG factory using HSQL syntax.
     * @throws SQLException if connection to the database failed.
     */
    protected AbstractAuthorityFactory createBackingStore(final Hints hints) throws SQLException {
        final DataSource source = getDataSource();
        final File directory    = getDirectory(source);
        final File propertyFile = new File(directory, DATABASE_NAME + ".properties");
        deleteIfOutdated(directory, propertyFile);
        Connection connection   = source.getConnection();
        if (!dataExists(connection)) {
            /*
             * HSQL has created automatically an empty database. We need to populate it.
             * Executes the SQL scripts bundled in the JAR. In theory, each line contains
             * a full SQL statement. For this plugin however, we have compressed "INSERT
             * INTO" statements using Compactor class in this package.
             */
            final Logger logger = Logging.getLogger(LOGGER);
            final LogRecord record = Loggings.format(Level.INFO,
                    LoggingKeys.CREATING_CACHED_EPSG_DATABASE_$1, VERSION);
            record.setLoggerName(logger.getName());
            logger.log(record);
            final Statement statement = connection.createStatement();
            try {
                final BufferedReader in = new BufferedReader(new InputStreamReader(
                        ThreadedHsqlEpsgFactory.class.getResourceAsStream(SQL_FILE), "ISO-8859-1"));
                StringBuilder insertStatement = null;
                String line;
                while ((line=in.readLine()) != null) {
                    line = line.trim();
                    final int length = line.length();
                    if (length != 0) {
                        if (line.startsWith("INSERT INTO")) {
                            /*
                             * We are about to insert many rows into a single table.
                             * The row values appear in next lines; the current line
                             * should stop right after the VALUES keyword.
                             */
                            insertStatement = new StringBuilder(line);
                            continue;
                        }
                        if (insertStatement != null) {
                            /*
                             * We are about to insert a row. Prepend the "INSERT INTO"
                             * statement and check if we will have more rows to insert
                             * after this one.
                             */
                            final int values = insertStatement.length();
                            insertStatement.append(line);
                            final boolean hasMore = (line.charAt(length-1) == ',');
                            if (hasMore) {
                                insertStatement.setLength(insertStatement.length()-1);
                            }
                            line = insertStatement.toString();
                            insertStatement.setLength(values);
                            if (!hasMore) {
                                insertStatement = null;
                            }
                        }
                        statement.execute(line);
                    }
                }
                in.close();
                /*
                 * The database has been fully created. Now, make it read-only.
                 */
                if (directory != null) {
                    final InputStream propertyIn = new FileInputStream(propertyFile);
                    final Properties properties  = new Properties();
                    properties.load(propertyIn);
                    propertyIn.close();
                    properties.put("epsg.version", VERSION.toString());
                    properties.put("readonly", "true");
                    final OutputStream out = new FileOutputStream(propertyFile);
                    properties.store(out, "EPSG database on HSQL");
                    out.close();

                    final File backup = new File(directory, DATABASE_NAME + ".backup");
                    if (backup.exists()) {
                        backup.delete();
                    }
                }
            } catch (IOException exception) {
                statement.close();
                SQLException e = new SQLException(Errors.format(ErrorKeys.CANT_READ_$1, SQL_FILE));
                e.initCause(exception); // TODO: inline cause when we will be allowed to target Java 6.
                throw e;
            }
            statement.close();
            connection.close();
            connection = source.getConnection();
            assert dataExists(connection);
        }
        return new FactoryUsingHSQL(hints, connection);
    }
}
