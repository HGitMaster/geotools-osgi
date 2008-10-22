/*
 *    GeoLBS - OpenSource Location Based Servces toolkit
 *    (C) 2004, Julian J. Ray, All Rights Reserved
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
 *
 */

package org.geotools.data.geomedia;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.geotools.data.DataSourceException;
import org.geotools.data.Transaction;
import org.geotools.data.geomedia.attributeio.GeoMediaAttributeIO;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.JDBCDataStore;
import org.geotools.data.jdbc.JDBCDataStoreConfig;
import org.geotools.data.jdbc.JDBCUtils;
import org.geotools.data.jdbc.QueryData;
import org.geotools.data.jdbc.attributeio.AttributeIO;
import org.geotools.feature.AttributeTypeBuilder;
import org.opengis.feature.type.AttributeDescriptor;

import com.vividsolutions.jts.geom.Geometry;


/**
 * Geomedia data store implementation
 * 
 * @todo really fix and test this datastore, for the moment it's just a way
 * to make it compile
 *
 * @author Julian J. Ray
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/geomedia/src/main/java/org/geotools/data/geomedia/GeoMediaDataStore.java $
 * @version 1.0
 */
public class GeoMediaDataStore extends JDBCDataStore {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.data.geomedia");

    /** Maps SQL Server SQL types to Java classes */
    private static final Map TYPE_MAPPINGS = new HashMap();

    static {
        TYPE_MAPPINGS.put("bigint", Long.class);
        TYPE_MAPPINGS.put("bigint identity", Long.class);
        TYPE_MAPPINGS.put("int", Integer.class);
        TYPE_MAPPINGS.put("int identity", Integer.class);
        TYPE_MAPPINGS.put("smallint", Integer.class);
        TYPE_MAPPINGS.put("smallint identity", Integer.class);
        TYPE_MAPPINGS.put("char", Byte.class);
        TYPE_MAPPINGS.put("decimal", Double.class);
        TYPE_MAPPINGS.put("float", Float.class);
        TYPE_MAPPINGS.put("money", Double.class);
        TYPE_MAPPINGS.put("numeric", Double.class);
        TYPE_MAPPINGS.put("real", Double.class);
        TYPE_MAPPINGS.put("varchar", String.class);
        TYPE_MAPPINGS.put("nvarchar", String.class);
        TYPE_MAPPINGS.put("nchar", String.class);
    }

    // Used to cache GFeature table to remove need for subsequent reads
    private Hashtable mGFeatureCache = null;

    /**
     * Creates a new GeoMediaDataStore instance
     *
     * @param connectionPool ConnectionPool
     *
     * @throws IOException
     */
    public GeoMediaDataStore(DataSource dataSource)
        throws IOException {
        super(dataSource, new JDBCDataStoreConfig());

        mGFeatureCache = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws DataSourceException
     */

    /*
       public GeoMediaDataStore(ConnectionPool connectionPool, String namespace, String databaseName) throws IOException
             {
                 super(connectionPool, namespace, databaseName);
                 mGFeatureCache = null;
             }
     */

    /**
     * Reads the GeoMedia GFeatures table and caches metadata information. If the metadata has already been read, it is
     * overwritten. Used to update the DataStore if changes have been made by GeoMedia.
     *
     * @throws DataSourceException
     */
    public void readMetadata() throws DataSourceException {
        // See if this is the first time reading metadata
        if (mGFeatureCache == null) {
            mGFeatureCache = new Hashtable();

            // Empty existing cache if populated
        }

        if (mGFeatureCache.size() > 0) {
            mGFeatureCache.clear();
        }

        Connection conn = null;
        ResultSet  rs = null;
        Statement  stmt = null;

        try {
            conn = getConnection(Transaction.AUTO_COMMIT);

            String sql = "SELECT featurename, geometrytype, primarygeometryfieldname, featuredescription FROM gfeatures";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                GFeatureType gFeature = new GFeatureType();
                gFeature.setTypeName(rs.getString(1));
                gFeature.setGeoMediaFeatureType(rs.getInt(2));
                gFeature.setGeomColName(rs.getString(3));
                gFeature.setDescription(rs.getString(4));

                mGFeatureCache.put(gFeature.getTypeName().toUpperCase(), gFeature);
            }
        } catch (IOException e) {
            LOGGER.warning("IOException reading metadata: " + e.getMessage());
            throw new DataSourceException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.warning("SQLException reading metadata: " + e.getMessage());
            throw new DataSourceException(e.getMessage());
        } finally {
            JDBCUtils.close(rs);
            JDBCUtils.close(stmt);
            JDBCUtils.close(conn, null, null);
        }
    }

    /**
     * Returns feature tables which have been entered into the GeoMedia metadata table.
     *
     * @param tablename String - the table to test
     *
     * @return boolean - true if the table is a spatial table, false otherwise.
     */
    protected boolean allowTable(String tablename) {
        // If the metadata has not been initialized then we attempt to do it here. Note that
        // we are swallowing any exceptions which are thrown
        if (mGFeatureCache == null) {
            try {
                readMetadata();
            } catch (IOException e) {
                return false;
            }
        }

        // This just allows feature tables which are stored in the GFeature metadata table
        boolean res = mGFeatureCache.containsKey(tablename.toUpperCase());

        return res;
    }

    /**
     * Overrides the buildAttributeType method to check for GDO_GEOMETRY columns. This function fromes any GeoMedia
     * managed columns which do not contain spatial data such as GDO_GEOMETRY_XHI and spatial indexes.
     *
     * @param rs ResultSet
     *
     * @return AttributeType
     *
     * @throws SQLException
     * @throws DataSourceException
     *
     * @see org.geolbs.data.jdbc.JDBCDataStore#buildAttributeType(java.sql.ResultSet)
     */
    protected AttributeDescriptor buildAttributeType(ResultSet rs)
        throws IOException {
        try {
            final int TABLE_NAME = 3;
            final int COLUMN_NAME = 4;

            // Need to check...
            if (mGFeatureCache == null) {
                readMetadata();
            }

            String columnName = rs.getString(COLUMN_NAME);
            String tableName = rs.getString(TABLE_NAME);

            // We have to check to see if this column name is in the GeoMedia GFeatures geometry col names
            GFeatureType gFeature = (GFeatureType) mGFeatureCache.get(tableName.toUpperCase());

            if (gFeature != null) {
                if (columnName.compareToIgnoreCase(gFeature.getGeomColName()) == 0) {
                    return new AttributeTypeBuilder().binding( Geometry.class ).buildDescriptor(columnName);
                }
            }

            // Filter out the GDO bounding box columns GDO_GEOMETRY_XHI, GDO_GEOMETRY_XLO, GDO_GEOMETRY_YHI and GDO_GEOMETRY_YLO
            if (columnName.startsWith("GDO_GEOMETRY") == true) {
                return null;
            }

            return super.buildAttributeType(rs);
        } catch (SQLException e) {
            throw new DataSourceException("Sql exception while parsing feature type", e);
        } 
    }

    /**
     * @see org.geotools.data.jdbc.JDBCDataStore#getGeometryAttributeIO(org.geotools.feature.AttributeType, org.geotools.data.jdbc.QueryData)
     */
    protected AttributeIO getGeometryAttributeIO(AttributeDescriptor type, QueryData queryData) throws IOException {
        return new GeoMediaAttributeIO();
    }

    

    
}
