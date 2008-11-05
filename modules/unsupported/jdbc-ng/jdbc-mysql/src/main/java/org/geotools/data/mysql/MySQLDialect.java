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
package org.geotools.data.mysql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.SQLDialect;
import org.opengis.feature.type.GeometryDescriptor;

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
import com.vividsolutions.jts.io.WKBReader;

/**
 * Delegate for {@link MySQLDialectBasic} and {@link MySQLDialectPrepared}
 * which implements the common part of the api. 
 * 
 * @author Justin Deoliveira, OpenGEO
 *
 */
public class MySQLDialect extends SQLDialect {
    /**
     * mysql spatial types
     */
    protected Integer POINT = new Integer(2001);
    protected Integer LINESTRING = new Integer(2002);
    protected Integer POLYGON = new Integer(2003);
    protected Integer MULTIPOINT = new Integer(2004);
    protected Integer MULTILINESTRING = new Integer(2005);
    protected Integer MULTIPOLYGON = new Integer(2006);
    protected Integer GEOMETRY = new Integer(2007);

    
    public MySQLDialect(JDBCDataStore dataStore) {
        super(dataStore);
    }

    public String getNameEscape() {
        return "";
    }

    public String getGeometryTypeName(Integer type) {
        if (POINT.equals(type)) {
            return "POINT";
        }

        if (MULTIPOINT.equals(type)) {
            return "MULTIPOINT";
        }

        if (LINESTRING.equals(type)) {
            return "LINESTRING";
        }

        if (MULTILINESTRING.equals(type)) {
            return "MULTILINESTRING";
        }

        if (POLYGON.equals(type)) {
            return "POLYGON";
        }

        if (MULTIPOLYGON.equals(type)) {
            return "MULTIPOLYGON";
        }

        if (GEOMETRY.equals(type)) {
            return "GEOMETRY";
        }

        return super.getGeometryTypeName(type);
    }

    public Integer getGeometrySRID(String schemaName, String tableName, String columnName,
        Connection cx) throws SQLException {
        //execute SELECT srid(<columnName>) FROM <tableName> LIMIT 1;
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT srid(");
        encodeColumnName(columnName, sql);
        sql.append(") ");
        sql.append("FROM ");

        if (schemaName != null) {
            encodeTableName(schemaName, sql);
            sql.append(".");
        }

        encodeSchemaName(tableName, sql);
        sql.append(" WHERE ");
        encodeColumnName(columnName, sql);
        sql.append(" is not null LIMIT 1");

        dataStore.getLogger().fine(sql.toString());

        Statement st = cx.createStatement();

        try {
            ResultSet rs = st.executeQuery(sql.toString());

            try {
                if (rs.next()) {
                    return new Integer(rs.getInt(1));
                } else {
                    //could not find out
                    return null;
                }
            } finally {
                dataStore.closeSafe(rs);
            }
        } finally {
            dataStore.closeSafe(st);
        }
    }

    public void encodeColumnType(String sqlTypeName, StringBuffer sql) {
        if ("VARCHAR".equalsIgnoreCase(sqlTypeName)) {
            sql.append("VARCHAR(255)");
        } else {
            super.encodeColumnType(sqlTypeName, sql);
        }
    }

    public void encodeGeometryColumn(GeometryDescriptor gatt, int srid, StringBuffer sql) {
        sql.append("asWKB(");
        encodeColumnName(gatt.getLocalName(), sql);
        sql.append(")");
    }

    public void encodeGeometryEnvelope(String tableName, String geometryColumn, StringBuffer sql) {
        sql.append("asWKB(");
        sql.append("envelope(");
        encodeColumnName(geometryColumn, sql);
        sql.append("))");
    }

    public Envelope decodeGeometryEnvelope(ResultSet rs, int column,
                Connection cx) throws SQLException, IOException {
        //String wkb = rs.getString( column );
        byte[] wkb = rs.getBytes(column);

        try {
            //TODO: srid
            Polygon polygon = (Polygon) new WKBReader().read(wkb);

            return polygon.getEnvelopeInternal();
        } catch (ParseException e) {
            String msg = "Error decoding wkb for envelope";
            throw (IOException) new IOException(msg).initCause(e);
        }
    }

    public Geometry decodeGeometryValue(GeometryDescriptor descriptor, ResultSet rs, String name,
        GeometryFactory factory, Connection cx ) throws IOException, SQLException {
        byte[] bytes = rs.getBytes(name);
        if ( bytes == null ) {
            return null;
        }
        try {
            return new WKBReader(factory).read(bytes);
        } catch (ParseException e) {
            String msg = "Error decoding wkb";
            throw (IOException) new IOException(msg).initCause(e);
        }
    }

    public void registerClassToSqlMappings(Map<Class<?>, Integer> mappings) {
        super.registerClassToSqlMappings(mappings);

        mappings.put(Point.class, POINT);
        mappings.put(LineString.class, LINESTRING);
        mappings.put(Polygon.class, POLYGON);
        mappings.put(MultiPoint.class, MULTIPOINT);
        mappings.put(MultiLineString.class, MULTILINESTRING);
        mappings.put(MultiPolygon.class, MULTIPOLYGON);
        mappings.put(Geometry.class, GEOMETRY);
    }

    public void registerSqlTypeToClassMappings(Map<Integer, Class<?>> mappings) {
        super.registerSqlTypeToClassMappings(mappings);

        mappings.put(POINT, Point.class);
        mappings.put(LINESTRING, LineString.class);
        mappings.put(POLYGON, Polygon.class);
        mappings.put(MULTIPOINT, MultiPoint.class);
        mappings.put(MULTILINESTRING, MultiLineString.class);
        mappings.put(MULTIPOLYGON, MultiPolygon.class);
        mappings.put(GEOMETRY, Geometry.class);
    }

    public void registerSqlTypeNameToClassMappings(Map<String, Class<?>> mappings) {
        super.registerSqlTypeNameToClassMappings(mappings);

        mappings.put("POINT", Point.class);
        mappings.put("LINESTRING", LineString.class);
        mappings.put("POLYGON", Polygon.class);
        mappings.put("MULTIPOINT", MultiPoint.class);
        mappings.put("MULTILINESTRING", MultiLineString.class);
        mappings.put("MULTIPOLYGON", MultiPolygon.class);
        mappings.put("GEOMETRY", Geometry.class);
        mappings.put("GEOMETRYCOLLETION", GeometryCollection.class);
    }

    public void encodePostCreateTable(String tableName, StringBuffer sql) {
        //TODO: make this configurable
        sql.append("ENGINE=InnoDB");
    }

    public void encodePrimaryKey(String column, StringBuffer sql) {
        encodeColumnName(column, sql);
        sql.append(" int AUTO_INCREMENT PRIMARY KEY");
    }

    @Override
    public Object getNextAutoGeneratedValue(String schemaName,
            String tableName, String columnName, Connection cx)
            throws SQLException {
        //TODO: we need to find some way to make use of mysql's last_insert_id() function
        // but that only works if we do it post-insert...and this method is supposed to get 
        // the next value pre-insert
        Statement st = cx.createStatement();
        try {
            String sql = "SELECT max( " + columnName + ")+1" + " FROM " + tableName;
            dataStore.getLogger().fine(sql);
    
            ResultSet rs = st.executeQuery(sql);
    
            try {
                rs.next();
    
                return new Integer(rs.getInt(1));
            } finally {
                dataStore.closeSafe(rs);
            }
        } finally {
            dataStore.closeSafe(st);
        }
    }

}
