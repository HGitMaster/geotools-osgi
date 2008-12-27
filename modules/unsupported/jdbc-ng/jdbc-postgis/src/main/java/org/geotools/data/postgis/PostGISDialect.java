/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.postgis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.PreparedStatementSQLDialect;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class PostGISDialect extends PreparedStatementSQLDialect {

    final static Map<String, Class> GEOM_TYPE_MAP = new HashMap<String, Class>() {
        {
            put("GEOMETRY", Geometry.class);
            put("POINT", Point.class);
            put("POINTM", Point.class);
            put("LINESTRING", LineString.class);
            put("LINESTRINGM", LineString.class);
            put("POLYGON", Polygon.class);
            put("POLYGONM", Polygon.class);
            put("MULTIPOINT", MultiPoint.class);
            put("MULTIPOINTM", MultiPoint.class);
            put("MULTILINESTRING", MultiLineString.class);
            put("MULTILINESTRINGM", MultiLineString.class);
            put("MULTIPOLYGON", MultiPolygon.class);
            put("MULTIPOLYGONM", MultiPolygon.class);
            put("GEOMETRYCOLLECTION", GeometryCollection.class);
            put("GEOMETRYCOLLECTIONM", GeometryCollection.class);
        }
    };

    boolean looseBBOXEnabled = true;
    
    boolean estimatedExtentsEnabled = false;

    public boolean isLooseBBOXEnabled() {
        return looseBBOXEnabled;
    }
    
    public void setLooseBBOXEnabled(boolean looseBBOXEnabled) {
        this.looseBBOXEnabled = looseBBOXEnabled;
    }

    public PostGISDialect(JDBCDataStore dataStore) {
        super(dataStore);
    }

    @Override
    public Geometry decodeGeometryValue(GeometryDescriptor descriptor,
            ResultSet rs, String column, GeometryFactory factory, Connection cx)
            throws IOException, SQLException {
        return (Geometry) new WKBAttributeIO(factory).read(rs, column);
    }
    
    @Override
    public void setGeometryValue(Geometry g, int srid, Class binding,
            PreparedStatement ps, int column) throws SQLException {
    }
    
    
    @Override
    public void encodeGeometryColumn(GeometryDescriptor gatt, int srid,
            StringBuffer sql) {
        CoordinateReferenceSystem crs = gatt.getCoordinateReferenceSystem();
        int dimensions = crs == null ? 2 : crs.getCoordinateSystem().getDimension();
        sql.append("encode(");
        if( dimensions > 2 ){
            sql.append("asEWKB(");
        }
        else {
            sql.append("asBinary(");
        }
        encodeColumnName(gatt.getLocalName(), sql);
        sql.append(",'XDR'),'base64')");
    }

    @Override
    public void encodeGeometryEnvelope(String tableName, String geometryColumn, StringBuffer sql) {
        if (estimatedExtentsEnabled) {
            sql.append("estimated_extent(");
            sql.append("'" + tableName + "','" + geometryColumn + "'))));");
        } else {
            sql.append("AsText(force_2d(Envelope(");
            sql.append("Extent(\"" + geometryColumn + "\"))))");
        }
    }
    
    @Override
    public Envelope decodeGeometryEnvelope(ResultSet rs, int column,
            Connection cx) throws SQLException, IOException {
        try {
            return new WKTReader().read(rs.getString(column)).getEnvelopeInternal();
        } catch (ParseException e) {
            throw (IOException) new IOException("Error occurred parsing the bounds WKT").initCause(e);
        }
    }

    @Override
    public Class<?> getMapping(ResultSet columnMetaData, Connection cx)
            throws SQLException {
        final int SCHEMA_NAME = 2;
        final int TABLE_NAME = 3;
        final int COLUMN_NAME = 4;
        final int TYPE_NAME = 6;
        if (!columnMetaData.getString(TYPE_NAME).equals("geometry")) {
            return null;
        }
        
        // grab the information we need to proceed
        String tableName = columnMetaData.getString(TABLE_NAME);
        String columnName = columnMetaData.getString(COLUMN_NAME);
        String schemaName = columnMetaData.getString(SCHEMA_NAME);

        // first attempt, try with the geometry metadata
        Connection conn = null;
        Statement statement = null;
        ResultSet result = null;
        String gType = null;
        try {
           String sqlStatement = "SELECT TYPE FROM GEOMETRY_COLUMNS WHERE " //
                    + "F_TABLE_SCHEMA = '" + schemaName + "' " //
                    + "AND F_TABLE_NAME = '" + tableName + "' " //
                    + "AND F_GEOMETRY_COLUMN = '" + columnName + "'";

            LOGGER.log(Level.FINE, "Geometry type check; {0} ", sqlStatement);
            statement = cx.createStatement();
            result = statement.executeQuery(sqlStatement);

            if (result.next()) {
                gType = result.getString(1);
            }
        } finally {
            dataStore.closeSafe(result);
            dataStore.closeSafe(statement);
        }
        
        // TODO: add the support code needed to infer from the first geometry
//        if (gType == null) {
//            // no geometry_columns entry, try grabbing a feature
//            StringBuffer sql = new StringBuffer();
//            sql.append("SELECT encode(AsBinary(force_2d(\"");
//            sql.append(columnName);
//            sql.append("\"), 'XDR'),'base64') FROM \"");
//            sql.append(schemaName);
//            sql.append("\".\"");
//            sql.append(tableName);
//            sql.append("\" LIMIT 1");
//            result = statement.executeQuery(sql.toString());
//            if (result.next()) {
//                AttributeIO attrIO = getGeometryAttributeIO(null, null);
//                Object object = attrIO.read(result, 1);
//                if (object instanceof Geometry) {
//                    Geometry geom = (Geometry) object;
//                    geometryType = geom.getGeometryType().toUpperCase();
//                    type = geom.getClass();
//                    srid = geom.getSRID(); // will return 0 unless we support
//                                           // EWKB
//                }
//            }
//            result.close();
//        }
//        statement.close();

        // decode the type into
        Class geometryClass = (Class) GEOM_TYPE_MAP.get(gType);
        if (geometryClass == null)
            geometryClass = Geometry.class;

        return geometryClass;
    }
    
    @Override
    public Integer getGeometrySRID(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {

        // first attempt, try with the geometry metadata
        Connection conn = null;
        Statement statement = null;
        ResultSet result = null;
        Integer srid = null;
        try {
           String sqlStatement = "SELECT SRID FROM GEOMETRY_COLUMNS WHERE " //
                    + "F_TABLE_SCHEMA = '" + schemaName + "' " //
                    + "AND F_TABLE_NAME = '" + tableName + "' " //
                    + "AND F_GEOMETRY_COLUMN = '" + columnName + "'";

            LOGGER.log(Level.FINE, "Geometry type check; {0} ", sqlStatement);
            statement = cx.createStatement();
            result = statement.executeQuery(sqlStatement);

            if (result.next()) {
                srid = result.getInt(1);
            }
        } finally {
            dataStore.closeSafe(result);
            dataStore.closeSafe(statement);
        }
        
        // TODO: implement inference from the first feature
      //try asking the first feature for its srid
//        sql = new StringBuffer();
//        sql.append("SELECT SRID(\"");
//        sql.append(geometryColumnName);
//        sql.append("\") FROM \"");
//        if (schemaEnabled && dbSchema != null && dbSchema.length() > 0) {
//            sql.append(dbSchema);
//            sql.append("\".\"");
//        }
//        sql.append(tableName);
//        sql.append("\" LIMIT 1");
//        sqlStatement = sql.toString();
//        result = statement.executeQuery(sqlStatement);
//        if (result.next()) {
//            int retSrid = result.getInt(1);
//            JDBCUtils.close(statement);
//            return retSrid;
//        }
        
        return srid;
    }

}
