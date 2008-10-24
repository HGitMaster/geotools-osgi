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
package org.geotools.data.h2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.SQLDialect;
import org.opengis.feature.type.GeometryDescriptor;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.OutputStreamOutStream;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTWriter;


public class H2Dialect extends SQLDialect {
    
    public H2Dialect( JDBCDataStore dataStore ) {
        super( dataStore );
    }
    
    public String getNameEscape() {
        return "\"";
    }

    public void registerSqlTypeToClassMappings(Map<Integer, Class<?>> mappings) {
        super.registerSqlTypeToClassMappings(mappings);

        //geometries
        //mappings.put(new Integer(Types.OTHER), Geometry.class);
        mappings.put(new Integer(Types.BLOB), Geometry.class);
    }

    public void registerClassToSqlMappings(Map<Class<?>, Integer> mappings) {
        super.registerClassToSqlMappings(mappings);

        //geometries
        /*
           mappings.put(Geometry.class, new Integer(Types.OTHER));
           mappings.put(Point.class, new Integer(Types.OTHER));
           mappings.put(LineString.class, new Integer(Types.OTHER));
           mappings.put(Polygon.class, new Integer(Types.OTHER));
         */

        //TODO: only map geometry?
        mappings.put(Geometry.class, new Integer(Types.BLOB));
        mappings.put(Point.class, new Integer(Types.BLOB));
        mappings.put(LineString.class, new Integer(Types.BLOB));
        mappings.put(Polygon.class, new Integer(Types.BLOB));
    }

    public Integer getGeometrySRID(String schemaName, String tableName, String columnName,
        Connection cx) throws SQLException {
        //execute SELECT srid(<columnName>) FROM <tableName> LIMIT 1;
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT getSRID(");
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

    public void encodeGeometryEnvelope(String tableName, String geometryColumn, StringBuffer sql) {
        //TODO: change spatialdbbox to use envelope
        sql.append("envelope(");
        encodeColumnName(geometryColumn, sql);
        sql.append(")");
    }

    @Override
    public Envelope decodeGeometryEnvelope(ResultSet rs, int column,
            Connection cx) throws SQLException, IOException {
        
        //TODO: change spatialdb in a box to return ReferencedEnvelope
        return (Envelope) rs.getObject(column);
    }

    public void encodeGeometryValue(Geometry value, int srid, StringBuffer sql)
        throws IOException {
        sql.append("GeomFromText ('");
        sql.append(new WKTWriter().write(value));
        sql.append("',");
        sql.append(srid);
        sql.append(")");
    }
    
    @Override
    public void setGeometryValue(Geometry g, int srid,
            Class binding, PreparedStatement ps, int column)
            throws SQLException {
        if ( g == null ) {
            ps.setNull( column, Types.BLOB );
            return;
        }
        
        WKBWriter w = new WKBWriter();
        
        // write the geometry
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            w.write( g , new OutputStreamOutStream( bytes ) );
       
            //supplement it with the srid
            bytes.write( (byte)(srid >>> 24) );
            bytes.write( (byte)(srid >> 16 & 0xff) );
            bytes.write( (byte)(srid >> 8 & 0xff) );
            bytes.write( (byte)(srid & 0xff) );
            
            ps.setBytes( column, bytes.toByteArray() );
        } catch(IOException e) {
            throw (SQLException) new SQLException("A problem occurred " +
            		"while serializing the geometry").initCause(e);
        }
    }

    public Geometry decodeGeometryValue(GeometryDescriptor descriptor, ResultSet rs, String column,
        GeometryFactory factory, Connection cx ) throws IOException, SQLException {
        byte[] bytes = rs.getBytes(column);

        if (bytes == null) {
            return null;
        }

        try {
            return new WKBReader(factory).read(bytes);
        } catch (ParseException e) {
            throw (IOException) new IOException().initCause(e);
        }

        //return JTS.geometryFromBytes( bytes );
    }

    public void encodePrimaryKey(String column, StringBuffer sql) {
        encodeColumnName(column, sql);
        sql.append(" int AUTO_INCREMENT(1) PRIMARY KEY");
    }

    @Override
    public String getSequenceForColumn(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {
        
        String sequenceName = tableName + "_" + columnName + "_SEQUENCE"; 
        
        //sequence names have to be upper case to select values from them
        sequenceName = sequenceName.toUpperCase();
        Statement st = cx.createStatement();
        try {
            StringBuffer sql = new StringBuffer();
            sql.append( "SELECT * FROM INFORMATION_SCHEMA.SEQUENCES ");
            sql.append( "WHERE SEQUENCE_NAME = '").append( sequenceName ).append( "'" );
            
            dataStore.getLogger().fine( sql.toString() );
            ResultSet rs = st.executeQuery( sql.toString() );
            try {
                if ( rs.next() ) {
                    return sequenceName;
                }
            } finally {
                dataStore.closeSafe(rs);
            }
        }
        finally {
            dataStore.closeSafe(st);
        }
        
        return null;
    }
    
    @Override
    public Object getNextSequenceValue(String schemaName, String sequenceName,
            Connection cx) throws SQLException {
        
        Statement st = cx.createStatement();
        try {
            String sql = "SELECT NEXTVAL('" + sequenceName + "')";
            dataStore.getLogger().fine( sql );
            ResultSet rs = st.executeQuery( sql );
            try {
                rs.next();
                return rs.getInt( 1 );
            }
            finally {
                dataStore.closeSafe( rs );
            }
            
        }
        finally {
            dataStore.closeSafe( st );
        }
    }
    
    @Override
    public Object getNextAutoGeneratedValue(String schemaName,
            String tableName, String columnName, Connection cx)
            throws SQLException {
        
        Statement st = cx.createStatement();
        try {
            ResultSet rs = st.executeQuery("SELECT b.COLUMN_DEFAULT "
                    + " FROM INFORMATION_SCHEMA.INDEXES A, INFORMATION_SCHEMA.COLUMNS B "
                    + "WHERE a.TABLE_NAME = b.TABLE_NAME " + " AND a.COLUMN_NAME = b.COLUMN_NAME "
                    + " AND a.TABLE_NAME = '" + tableName + "' " + " AND a.COLUMN_NAME = '"
                    + columnName + "' " + " AND a.PRIMARY_KEY = TRUE");

            //figure out which sequence to query
            String sequence = null;

            try {
                //TODO: there has to be a better way to do this
                rs.next();

                String string = rs.getString(1);
                sequence = string.substring(string.indexOf("SYSTEM_SEQUENCE"), string.length() - 1);
            } finally {
                dataStore.closeSafe(rs);
            }

            try {
                if (schemaName != null) {
                    rs = st.executeQuery("SELECT CURRVAL('" + schemaName + "','" + sequence + "')");
                } else {
                    rs = st.executeQuery("SELECT CURRVAL('" + sequence + "')");
                }

                rs.next();

                int value = rs.getInt(1);

                return new Integer(value + 1);
            } finally {
                dataStore.closeSafe(rs);
            }
        } finally {
            dataStore.closeSafe(st);
        }
    }
}
