/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *    (C) Copyright IBM Corporation, 2005-2007. All rights reserved.
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

package org.geotools.data.db2;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTWriter;

public class DB2Util {

	private static Map<Class, String> PARAMETER_LITERALS = null;
	private static Map<Class, String> PARAMETER_MARKES = null;

	private static String quote(String s) {
		return "\""+s+"\"";
	}

	public static void executeRegister(String schemaName,String tableName,String columnName, String srsName, Connection con) throws SQLException {
		String stmt = "call db2gse.ST_register_spatial_column(?,?,?,?,?,?)";
		
	    String s = "{" + stmt + "}";
	    CallableStatement ps = con.prepareCall(s);
		ps.setString(1, quote(schemaName));
		ps.setString(2, quote(tableName));
		ps.setString(3, quote(columnName));
		ps.setString(4, srsName);
	    ps.registerOutParameter(5, Types.INTEGER);
	    ps.registerOutParameter(6, Types.CHAR);
	    ps.executeUpdate();
	    //DB2TestSetup.LOGGER.log(Level.INFO,ps.getInt(5) + "|" + ps.getString(6));
	}

	public static void executeUnRegister(String schemaName,String tableName,String columnName,Connection con) throws SQLException {
		
	
		String stmt =  "call db2gse.ST_unregister_spatial_column(?,?,?,?,?)";
	
		String s = "{" + stmt + "}";
		CallableStatement ps = con.prepareCall(s);
		ps.setString(1, quote(schemaName));
		ps.setString(2, quote(tableName));
		ps.setString(3, quote(columnName));
		ps.registerOutParameter(4, Types.INTEGER);
		ps.registerOutParameter(5, Types.CHAR);
		ps.executeUpdate();
		//DB2TestSetup.LOGGER.log(Level.INFO,ps.getInt(4) + "|" + ps.getString(5));
		
		
	}

	static {
		PARAMETER_MARKES = new HashMap<Class,String>();
		PARAMETER_MARKES.put(Point.class, "DB2GSE.ST_PointFromWKB(cast (? as BLOB(2G)),{0})");
		PARAMETER_MARKES.put(LineString.class, "DB2GSE.ST_LineFromWKB(cast (? as BLOB(2G)),{0})");
		PARAMETER_MARKES.put(Polygon.class, "DB2GSE.ST_PolyFromWKB(cast (? as BLOB(2G)),{0})");
		PARAMETER_MARKES.put(MultiPoint.class, "DB2GSE.ST_MPointFromWKB(cast (? as BLOB(2G)),{0})");
		PARAMETER_MARKES.put(MultiLineString.class, "DB2GSE.ST_MLineFromWKB(cast (? as BLOB(2G)),{0})");
		PARAMETER_MARKES.put(MultiPolygon.class, "DB2GSE.ST_MPolyFromWKB(cast (? as BLOB(2G)),{0})");
		PARAMETER_MARKES.put(Geometry.class, "DB2GSE.ST_GeomFromWKB(cast (? as BLOB(2G)),{0})");
		PARAMETER_MARKES.put(GeometryCollection.class, "DB2GSE.ST_GeomCollFromWKB(cast (? as BLOB(2G)),{0})");
		
		PARAMETER_LITERALS = new HashMap<Class,String>();
		PARAMETER_LITERALS.put(Point.class, "DB2GSE.ST_PointFromText({0},{1})");
		PARAMETER_LITERALS.put(LineString.class, "DB2GSE.ST_LineFromText({0},{1})");
		PARAMETER_LITERALS.put(Polygon.class, "DB2GSE.ST_PolyFromText({0},{1})");
		PARAMETER_LITERALS.put(MultiPoint.class, "DB2GSE.ST_MPointFromText({0},{1})");
		PARAMETER_LITERALS.put(MultiLineString.class, "DB2GSE.ST_MLineFromText({0},{1})");
		PARAMETER_LITERALS.put(MultiPolygon.class, "DB2GSE.ST_MPolyFromText({0},{1})");
		PARAMETER_LITERALS.put(Geometry.class, "DB2GSE.ST_GeomFromText({0},{1})");
		PARAMETER_LITERALS.put(GeometryCollection.class, "DB2GSE.ST_GeomCollFromText({0},{1})");
		
	
	}

	static public void prepareGeometryValue(Geometry geom, int srid, Class binding, StringBuffer sql) {
		String pattern = PARAMETER_MARKES.get(binding);
		sql.append(MessageFormat.format(pattern, new Object[]{Integer.toString(srid)}));		
	}

    static public void encodeGeometryValue(Geometry value, int srid, StringBuffer sql)
    throws IOException {
    	
		if (value ==null) {
			sql.append("null");
			return;
		}
		
		String pattern = PARAMETER_LITERALS.get(value.getClass());
		if (pattern==null) {//should not happen
			sql.append("null");
			return;
		}
		sql.append(MessageFormat.format(pattern, 
				new Object[]{				
				new WKTWriter().write(value),
				Integer.toString(srid)}));		
	
	}
	
}
