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
package org.geotools.data.db2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.logging.Level;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.SQLDialect;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;


public class DB2SQLDialect extends SQLDialect  {

	private static Integer GEOMETRY 		= 9001;
	private static Integer GEOMETRYCOLL 	= 9002;
	private static Integer POINT 			= 9003;
	private static Integer MULTIPOINT 		= 9004;
	private static Integer LINESTRING 		= 9005;
	private static Integer MULTILINESTRING	= 9006;
	private static Integer POLY 			= 9007;
	private static Integer MULTIPOLY		= 9008;
	
	private static String POINT_STR ="\"DB2GSE\".\"ST_POINT\"";
	private static String LINESTRING_STR ="\"DB2GSE\".\"ST_LINESTRING\"";
	private static String POLY_STR ="\"DB2GSE\".\"ST_POLYGON\"";
	private static String MULTIPOINT_STR ="\"DB2GSE\".\"ST_MULTIPOINT\"";
	private static String MULTILINESTRING_STR ="\"DB2GSE\".\"ST_MULTILINESTRING\"";
	private static String MULTIPOLY_STR ="\"DB2GSE\".\"ST_MULTIPOLYGON\"";
	private static String GEOMETRY_STR ="\"DB2GSE\".\"ST_GEOMETRY\"";
	private static String GEOMETRYCOLL_STR ="\"DB2GSE\".\"ST_GEOMCOLLECTION\"";

	
	
	private static String DEFAULT_SRS_NAME = "DEFAULT_SRS";
	private static Integer DEFAULT_SRS_ID=0;
	
    private static String SELECT_SRSID_WITH_SCHEMA = 
    	"select SRS_ID from DB2GSE.ST_GEOMETRY_COLUMNS where TABLE_SCHEMA = ? and "+
    	"TABLE_NAME = ? and COLUMN_NAME = ?";
    
    private static String SELECT_SRSID_WITHOUT_SCHEMA = 
    	"select SRS_ID from DB2GSE.ST_GEOMETRY_COLUMNS where  "+
    	"TABLE_NAME = ? and COLUMN_NAME = ?";
    private static String SELECT_CRS_WKT = 
    	"select definition,organization,organization_coordsys_id " +
    	"from db2gse.st_coordinate_systems " +
    	"where coordsys_name = (select coordsys_name from db2gse.st_spatial_reference_systems where srs_id=?)";
    private static String SELECT_SRS_NAME_FROM_ID = 
    	"select srs_name from db2gse.st_spatial_reference_systems where srs_id = ?";
    private String SELECT_SRS_NAME_FROM_ORG = 
    	"select srs_name,srs_id from db2gse.st_spatial_reference_systems where organization = ? and organization_coordsys_id=?";

    private static String SELECT_INCLUDE_WITH_SCHEMA ="select table_schema,table_name  from db2gse.st_geometry_columns where table_schema = ? and table_name=?";
    private static String SELECT_INCLUDE="select table_schema,table_name  from db2gse.st_geometry_columns where table_schema = current schema  and table_name=?";
    
    public DB2SQLDialect(JDBCDataStore dataStore) {
        super(dataStore);
    }
    
    /* (non-Javadoc)
     * @see org.geotools.jdbc.SQLDialect#createCRS(int, java.sql.Connection)
     * 
     * First, look up the wkt def for the srid, if not found return null
     * If we have a wkt def from db2, try to decode with CRS.parseWKT, on success return
     * the crs
     * 
     * If we cannot parse the WKT def, use the organization and organization coordsys id to parse
     * with CRS.decode(), on success return the crs
     * 
     * Otherwise, its time to give up and return null 
     */
    @Override
    public CoordinateReferenceSystem createCRS(int srid, Connection cx) throws SQLException {
    	PreparedStatement ps = cx.prepareStatement(SELECT_CRS_WKT);
    	ps.setInt(1, srid);
    	ResultSet rs = ps.executeQuery();
    	String org=null,  wkt=null;
    	int orgid=0;
    	
    	if (rs.next()) {
    		wkt=rs.getString(1);
    		org=rs.getString(2);
    		orgid=rs.getInt(3);
    	}
    	ps.close();
    	rs.close();
    	
    	if (wkt==null) return null; // nothing found
    	
        try {
        	return CRS.parseWKT(wkt);
            
        } catch(Exception e) {     
            if(LOGGER.isLoggable(Level.WARNING)) 
                LOGGER.log(Level.WARNING, "Could not decode db2 wkt definition for " + srid  );            	    	
        }
        try {
        	return CRS.decode(org+":" + orgid);        
        } catch(Exception e) {
            if(LOGGER.isLoggable(Level.WARNING)) 
                LOGGER.log(Level.WARNING, "Could not decode " + org+":"+orgid + " using the geotools database", e);
        }
       return null;    
    }
    

    
    @Override
    public void encodePrimaryKey(String column, StringBuffer sql) {
    	super.encodePrimaryKey(column, sql);
    	sql.append(" NOT NULL");
    }
    
    @Override
    public String getGeometryTypeName(Integer type) {

    	if (GEOMETRY.equals(type)) return GEOMETRY_STR;
    	if (GEOMETRYCOLL.equals(type)) return GEOMETRYCOLL_STR;
    	if (POINT.equals(type)) return POINT_STR;
    	if (MULTIPOINT.equals(type)) return MULTIPOINT_STR;
    	if (LINESTRING.equals(type)) return LINESTRING_STR;
    	if (MULTILINESTRING.equals(type)) return MULTILINESTRING_STR;
    	if (POLY.equals(type)) return POLY_STR;
    	if (MULTIPOLY.equals(type)) return MULTIPOLY_STR;
    	return null;
    	        
    }

    @Override
    public Integer getGeometrySRID(String schemaName, String tableName, String columnName,
        Connection cx) throws SQLException {
    	
    	Integer srid = null;
    	PreparedStatement stmt = null;
    	
    	try {
	    	if (schemaName!=null) {
	    		 stmt = cx.prepareStatement(SELECT_SRSID_WITH_SCHEMA);
	    		 stmt.setString(1, schemaName);
	    		 stmt.setString(2, tableName);
	    		 stmt.setString(3, columnName);
	    	} else {
	    		 stmt = cx.prepareStatement(SELECT_SRSID_WITHOUT_SCHEMA);
	    		 stmt.setString(1, tableName);
	    		 stmt.setString(2, columnName);    		 
	    	}
	    	
	    	ResultSet rs = null;
	    	try {
	    		rs = stmt.executeQuery();
	    		if (rs.next()) 
	    			srid=(Integer) rs.getObject(1);
	    		}
	    	finally {	
	    		dataStore.closeSafe(rs);
	    	}
    	} finally {
    		dataStore.closeSafe(stmt);
    	}	
    	return srid;
    }


    public void encodeGeometryColumn(GeometryDescriptor gatt, int srid, StringBuffer sql) {
        encodeGeometryColumn(gatt, sql);
    }

        
    public void encodeGeometryColumn(GeometryDescriptor gatt, StringBuffer sql) {
        sql.append("db2gse.ST_AsBinary(");
        encodeColumnName(gatt.getLocalName(), sql);
        sql.append(")");
    }

    @Override
    public void encodeGeometryEnvelope(String tableName,String geometryColumn, StringBuffer sql) {
        sql.append("db2gse.ST_AsBinary(");
        sql.append("db2gse.ST_Envelope(");
        encodeColumnName(geometryColumn, sql);
        sql.append("))");
    }

    @Override
    public Envelope decodeGeometryEnvelope(ResultSet rs, int column,
                Connection cx) throws SQLException, IOException {
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

    
    @Override
    public Geometry decodeGeometryValue(GeometryDescriptor descriptor, ResultSet rs, String name,
        GeometryFactory factory, Connection cx ) throws IOException, SQLException {
    	
        byte[] bytes = rs.getBytes(name);
        if (bytes==null) return null;
        
        try {
            return new WKBReader(factory).read(bytes);
        } catch (ParseException e) {
            String msg = "Error decoding wkb";
            throw (IOException) new IOException(msg).initCause(e);
        }
    }

    
    @Override
    public void registerClassToSqlMappings(Map<Class<?>, Integer> mappings) {
        super.registerClassToSqlMappings(mappings);
        
      mappings.put(Point.class, POINT);
      mappings.put(LineString.class, LINESTRING);
      mappings.put(LinearRing.class, LINESTRING);
      mappings.put(Polygon.class, POLY);
      mappings.put(MultiPoint.class, MULTIPOINT);
      mappings.put(MultiLineString.class, MULTILINESTRING);
      mappings.put(MultiPolygon.class, MULTIPOLY);
      mappings.put(Geometry.class, GEOMETRY);
      mappings.put(GeometryCollection.class, GEOMETRYCOLL);

    }

    @Override
    public void registerSqlTypeToClassMappings(Map<Integer, Class<?>> mappings) {
        super.registerSqlTypeToClassMappings(mappings);

        mappings.put(GEOMETRY, Geometry.class);
        mappings.put(GEOMETRYCOLL, GeometryCollection.class);
        mappings.put(POINT, Point.class);
        mappings.put(MULTIPOINT, MultiPoint.class);
        mappings.put(LINESTRING, LineString.class);
        mappings.put(MULTILINESTRING, MultiLineString.class);
        mappings.put(POLY, Polygon.class);
        mappings.put(MULTIPOLY, Polygon.class);
    }

    @Override
    public void registerSqlTypeNameToClassMappings(Map<String, Class<?>> mappings) {
        super.registerSqlTypeNameToClassMappings(mappings);
        
    	mappings.put(POINT_STR, Point.class);
    	mappings.put(LINESTRING_STR, LineString.class);
    	mappings.put(POLY_STR, Polygon.class);
    	mappings.put(MULTIPOINT_STR, MultiPoint.class);
    	mappings.put(MULTILINESTRING_STR, MultiLineString.class);
    	mappings.put(MULTIPOLY_STR, MultiPolygon.class);
    	mappings.put(GEOMETRY_STR, Geometry.class);
    	mappings.put(GEOMETRYCOLL_STR, GeometryCollection.class);


    }


    @Override
    public void postCreateTable(String schemaName, SimpleFeatureType featureType, Connection cx)
    throws SQLException {
    	
    	if (featureType.getGeometryDescriptor()==null)	// table without geometry
    		return;
    	
    	String tableName = featureType.getTypeName();
    	String columnName = featureType.getGeometryDescriptor().getName().toString();
    	    	
    	for (AttributeDescriptor attr: featureType.getAttributeDescriptors()) {    		
    		if (attr instanceof GeometryDescriptor) {
    			GeometryDescriptor gDescr = (GeometryDescriptor) attr;
    			String srsName = null;
    			Integer srsId = (Integer) gDescr.getUserData().get(JDBCDataStore.JDBC_NATIVE_SRID);
    			if (srsId!= null) {
    				PreparedStatement ps1 = cx.prepareStatement(SELECT_SRS_NAME_FROM_ID);
    				ps1.setInt(1, srsId);
    				ResultSet rs = ps1.executeQuery();
    				if (rs.next())
    					srsName=rs.getString(1);
    				rs.close();
    				ps1.close();    				
    			}
    			if (srsName == null && gDescr.getCoordinateReferenceSystem()!=null) {
    				for (ReferenceIdentifier ident : gDescr.getCoordinateReferenceSystem().getIdentifiers()) {    					        				
        				PreparedStatement ps1 = cx.prepareStatement(SELECT_SRS_NAME_FROM_ORG);
        				ps1.setString(1,ident.getCodeSpace());
        				ps1.setInt(2,new Integer(ident.getCode()));
        				ResultSet rs = ps1.executeQuery();
        				if (rs.next()) {
        					srsName=rs.getString(1);
        					srsId=rs.getInt(2);
        				}
        				rs.close();
        				ps1.close();
        			if (srsName!=null) break;	
    				}    				    				    				    				
    			}
    			if (srsName==null) {
    				srsName = DEFAULT_SRS_NAME;
    				srsId = DEFAULT_SRS_ID;
    			}
    			DB2Util.executeRegister(schemaName, tableName, columnName, srsName, cx);
    			gDescr.getUserData().put(JDBCDataStore.JDBC_NATIVE_SRID,srsId);
    		}
    		
    	}
    	    	
    }
        	
    @Override
    public String getSequenceForColumn(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {
    	
    	// TODO, hard stuff
    	String sequenceName = tableName + "_" + columnName + "_SEQUENCE";
    	StringBuffer sql = new StringBuffer("SELECT SEQNAME FROM SYSCAT.SEQUENCES WHERE ");
    	    	
    	if (schemaName!=null) {
    		sql.append("SEQSCHEMA ='");
    		sql.append(schemaName);
    		sql.append("' AND ");
    	}
    	sql.append("SEQNAME = '");
    	sql.append(sequenceName);
    	sql.append("'");
    	        
        Statement st = cx.createStatement();
        try {
            ResultSet rs = st.executeQuery(sql.toString());
            try {
                if ( rs.next() ) {
                    return sequenceName; 
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
    public Object getNextSequenceValue(String schemaName, String sequenceName,
            Connection cx) throws SQLException {
    	
    	StringBuffer sql = new StringBuffer("SELECT next value for ");
    	if (schemaName!=null) {
    		encodeSchemaName(schemaName, sql);
    		sql.append(".");
    	}	
    	encodeTableName(sequenceName, sql);
    	sql.append( " from sysibm.sysdummy1");
        Statement st = cx.createStatement();
        try {
            ResultSet rs = st.executeQuery(sql.toString());
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
	public boolean includeTable(String schemaName, String tableName, Connection cx) throws SQLException {
		
	
				
		PreparedStatement ps = null;
		if (schemaName!=null || schemaName.trim().length()>0) { 
			ps = cx.prepareStatement(SELECT_INCLUDE_WITH_SCHEMA); 
			ps.setString(1,schemaName);
			ps.setString(2,tableName);
		}	else {
			ps = cx.prepareStatement(SELECT_INCLUDE);
			ps.setString(1,tableName);
		}
			
		ResultSet rs = ps.executeQuery();
		boolean isGeomTable = rs.next();
		rs.close();
		ps.close();		
		return isGeomTable;
	}




}
