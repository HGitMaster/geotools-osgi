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

// J2SE dependencies
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.factory.AbstractAuthorityFactory;


/**
 * Connection to the EPSG database in MS-Access format using JDBC-ODBC bridge. The EPSG
 * database can be downloaded from <A HREF="http://www.epsg.org">http://www.epsg.org</A>.
 * The JDBC-ODBC bridge is a documented feature of Sun's J2SE distribution. See
 * <A HREF="http://java.sun.com/j2se/1.5/docs/guide/jdbc/bridge.html">New data source
 * implementations in the JDBC-ODBC bridge</A>.
 * <P>
 * Just having this class accessible in the classpath, together with the registration in
 * the {@code META-INF/services/} directory, is suffisient to get a working EPSG authority
 * factory backed by this database. Vendors can create a copy of this class, modify it and
 * bundle it with their own distribution if they want to connect their users to an other
 * database (for example a PostgreSQL database reachable on internet).
 *
 * @since 2.4
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/plugin/epsg-access/src/main/java/org/geotools/referencing/factory/epsg/ThreadedAccessEpsgFactory.java $
 * @version $Id: ThreadedAccessEpsgFactory.java 30656 2008-06-12 20:32:50Z acuster $
 * @author Martin Desruisseaux (IRD)
 */
public class ThreadedAccessEpsgFactory extends ThreadedEpsgFactory {
    /**
     * Creates a new instance of this factory.
     */
    public ThreadedAccessEpsgFactory() {
        this(null);
    }

    /**
     * Creates a new instance of this factory using the specified set of hints.
     */
    public ThreadedAccessEpsgFactory(final Hints hints) {
        super(hints, PRIORITY + 9);
    }

    /**
     * Returns a data source using the JDBC-ODBC bridge for the "EPSG" database.
     */
    @Override
    protected DataSource createDataSource() throws SQLException {
        DataSource candidate = super.createDataSource();
        if (candidate == null) {
            final sun.jdbc.odbc.ee.DataSource source = new sun.jdbc.odbc.ee.DataSource();
            source.setDatabaseName("EPSG");
            candidate = source;
        }
        return candidate;
    }

    /**
     * Returns the backing-store factory for MS-Access syntax.
     *
     * @param  hints A map of hints, including the low-level factories to use for CRS creation.
     * @return The EPSG factory using MS-Access syntax.
     * @throws SQLException if connection to the database failed.
     */
    protected AbstractAuthorityFactory createBackingStore(final Hints hints) throws SQLException {
        final DataSource source = getDataSource();
        final Connection connection;
        try {
            connection = source.getConnection();
        } catch (RuntimeException exception) {
            /*
             * This try...catch block should NOT be needed. We added it as a workaround because
             * the JDBC-ODBC bridge on Linux throws a NullPointerException when trying to log a
             * warning to the tracer.
             */
            SQLException e = new SQLException("Unexpected exception in JDBC data source.");
            e.initCause(exception);
            throw e;
        }
        return new FactoryUsingSQL(hints, connection);
    }
}
