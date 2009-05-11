package org.geotools.data.spatialite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.media.jai.NullOpImage;

import org.geotools.jdbc.BasicSQLDialect;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStore.GeometryType;
import org.geotools.referencing.CRS;
//import org.geotools.jdbc.JDBCDataStore.GeometryType;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import SQLite.Callback;
import SQLite.Database;
import SQLite.JDBC2y.JDBCConnection;

import com.sun.org.omg.CORBA.AttributeDescription;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Dialect for SpatiaLite embedded database.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class SpatiaLiteDialect extends BasicSQLDialect {

    public SpatiaLiteDialect(JDBCDataStore dataStore) {
        super(dataStore);
    }

    @Override
    public void initializeConnection(Connection cx) throws SQLException {
        PrintStream out = System.out;
        System.setOut( new PrintStream( new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
            
        }));
        
        Statement st = cx.createStatement();
        try {
            //load the spatial extensions
            st.execute( "SELECT load_extension('libspatialite.dylib')" );
            st.close();
            
            st = cx.createStatement();
            
            //determine if the spatial metadata tables need to be created
            boolean initSpatialMetaData = false;
            try {
                st.execute( "SELECT count(*) from geometry_columns");
            }catch( SQLException e ) {
                initSpatialMetaData = true;
            }
            
            if ( initSpatialMetaData ) {
                st.execute( "SELECT InitSpatialMetaData()");
                st.close();
                st = cx.createStatement();
            }
            
            //determine if the spatial ref sys table needs to be loaded
            boolean loadSpatialRefSys = false;
            ResultSet rs = st.executeQuery( "SELECT * FROM spatial_ref_sys");
            try {
                loadSpatialRefSys = !rs.next();
            }
            finally {
                dataStore.closeSafe( rs );
            }
            
            if ( loadSpatialRefSys ) {
                try {
                    BufferedReader in = new BufferedReader( new InputStreamReader( 
                        getClass().getResourceAsStream( "init_spatialite-2.3.sql") ) );
                    String line = null;
                    while( (line = in.readLine() ) != null ) {
                        st.execute( line );
                    }
                    
                    in.close();
                }
                catch( IOException e ) {
                    throw new RuntimeException( "Error reading spatial ref sys file", e );
                }
            }
        }
        finally {
            dataStore.closeSafe( st );
        }
        
        System.setOut( out );
    }
    
    @Override
    public Class<?> getMapping(ResultSet columnMetaData, Connection cx) throws SQLException {
        //the sqlite jdbc driver maps geometry type to varchar, so do a lookup
        // in the geometry_columns table
        String tbl = columnMetaData.getString( "TABLE_NAME");
        String col = columnMetaData.getString( "COLUMN_NAME");
        
        String sql = "SELECT type FROM geometry_columns " + 
            "WHERE f_table_name = '" + tbl + "' " + 
            "AND f_geometry_column = '" + col + "'";
        LOGGER.fine( sql );
        
        Statement st = cx.createStatement();
        try {
            ResultSet rs = st.executeQuery( sql );
            try {
                if ( rs.next() ) {
                    String type = rs.getString( "type" );
                    return GeometryType.type( type );
                }
            }
            finally {
                dataStore.closeSafe( rs );
            }
        }
        finally {
            dataStore.closeSafe( st );
        }
        
        return null;
    }
    
    @Override
    public void registerClassToSqlMappings(Map<Class<?>, Integer> mappings) {
        super.registerClassToSqlMappings(mappings);
        mappings.put( GeometryType.POINT.getType(), GeometryType.POINT.getSQLType() );
        mappings.put( GeometryType.LINESTRING.getType(), GeometryType.LINESTRING.getSQLType() );
        mappings.put( GeometryType.POLYGON.getType(), GeometryType.POLYGON.getSQLType() );
        mappings.put( GeometryType.MULTIPOINT.getType(), GeometryType.MULTIPOINT.getSQLType() );
        mappings.put( GeometryType.MULTILINESTRING.getType(), GeometryType.MULTILINESTRING.getSQLType() );
        mappings.put( GeometryType.MULTIPOLYGON.getType(), GeometryType.MULTIPOLYGON.getSQLType() );
        mappings.put( GeometryType.GEOMETRY.getType(), GeometryType.GEOMETRY.getSQLType() );
        mappings.put( GeometryType.GEOMETRYCOLLECTION.getType(), GeometryType.GEOMETRYCOLLECTION.getSQLType() );
    }
    
    @Override
    public String getGeometryTypeName(Integer type) {
        return GeometryType.string( type );
    }
    
    @Override
    public Integer getGeometrySRID(String schemaName, String tableName, String columnName,
            Connection cx) throws SQLException {
        String sql = "SELECT srid FROM geometry_columns " + 
            "WHERE f_table_name = '" + tableName + "' " + 
            "AND f_geometry_column = '" + columnName + "'";
        Statement st = cx.createStatement();
        try {
            LOGGER.fine( sql );
            ResultSet rs = st.executeQuery( sql );
            try {
                if ( rs.next() ) {
                    return Integer.valueOf( rs.getInt( 1 ) );
                }
            }
            finally {
                dataStore.closeSafe( rs );
            }
        }
        finally {
            dataStore.closeSafe( st );
        }
        
        return super.getGeometrySRID(schemaName, tableName, columnName, cx);
    }
    
    @Override
    public void encodeGeometryColumn(GeometryDescriptor gatt, int srid, StringBuffer sql) {
        sql.append( "AsText(");
        encodeColumnName( gatt.getLocalName(), sql);
        sql.append( ")||';").append(srid).append("'");
    }
    
    @Override
    public Geometry decodeGeometryValue(GeometryDescriptor descriptor, ResultSet rs, int column,
            GeometryFactory factory, Connection cx) throws IOException, SQLException {
        String string = rs.getString( column );
        if ( string == null || "".equals( string.trim() ) ) {
            return null;
        }
        
        String[] split = string.split( ";" );
        String wkt = split[0];
        try {
            return new WKTReader(factory).read( wkt );
        }
        catch( ParseException e ) {
            throw (IOException) new IOException().initCause( e );
        }
        
    }
    
    @Override
    public void encodeGeometryValue(Geometry value, int srid, StringBuffer sql) throws IOException {
        sql.append("GeomFromText('") .append( new WKTWriter().write( value ) ).append( "',")
            .append(srid).append(")");
    }

    @Override
    public Geometry decodeGeometryValue(GeometryDescriptor descriptor, ResultSet rs, String column,
            GeometryFactory factory, Connection cx) throws IOException, SQLException {
        return null;
    }
    
    @Override
    public void encodeGeometryEnvelope(String tableName, String geometryColumn, StringBuffer sql) {
        sql.append("asText(envelope(");
        encodeColumnName(geometryColumn, sql);
        sql.append( "))");
    }
    
    @Override
    public Envelope decodeGeometryEnvelope(ResultSet rs, int column, Connection cx)
            throws SQLException, IOException {
        String wkt = rs.getString( column );
        if ( wkt != null ) {
            try {
                return new WKTReader().read( wkt ).getEnvelopeInternal();
            } 
            catch (ParseException e) {
                throw (IOException) new IOException("Error decoding envelope bounds").initCause( e );
            }
        }
        
        return null;
    }
    
    @Override
    public void postCreateTable(String schemaName, SimpleFeatureType featureType, Connection cx)
            throws SQLException, IOException {
        //create any geometry columns entries after the fact
        for ( AttributeDescriptor ad : featureType.getAttributeDescriptors() ) {
            if ( ad instanceof GeometryDescriptor ) {
                GeometryDescriptor gd = (GeometryDescriptor) ad;
                StringBuffer sql = new StringBuffer( "INSERT INTO geometry_columns VALUES (");
                
                //table name
                sql.append( "'").append( featureType.getTypeName() ).append( "'," );
                
                //geometry name
                sql.append( "'").append( gd.getLocalName() ).append( "',");
                
                //type
                String gType = GeometryType.string( gd.getType().getBinding() ) ;
                if ( gType == null ) {
                    throw new IOException( "Unknown geometry type: " + gd.getType().getBinding() );
                }
                sql.append( "'").append( gType ).append( "',");
                
                //coord dimension
                sql.append( 2 ).append( ",");
                
                //srid 
                Integer epsgCode = null;
                if ( gd.getCoordinateReferenceSystem() != null ) {
                    CoordinateReferenceSystem crs = gd.getCoordinateReferenceSystem();
                    try {
                        epsgCode = CRS.lookupEpsgCode( crs , true );
                    } 
                    catch (Exception e) {}
                }
                if ( epsgCode == null ) {
                    throw new IOException( "Unable to find epsg code code.");
                }
                sql.append( epsgCode ).append( ",");
                
                //spatial index enabled
                sql.append( 0 ).append( ")");
                
                LOGGER.fine( sql.toString() );
                Statement st = cx.createStatement();
                try {
                    st.executeUpdate( sql.toString() );
                }
                finally {
                    dataStore.closeSafe( st );
                }
            }
        }
    }
}
