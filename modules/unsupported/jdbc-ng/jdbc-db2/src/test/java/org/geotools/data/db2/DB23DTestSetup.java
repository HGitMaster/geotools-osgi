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

import java.sql.Connection;

import org.geotools.jdbc.JDBC3DTestSetup;

public class DB23DTestSetup extends JDBC3DTestSetup {
	
	private static int SRID = 4359;
	private static String SRSNAME = "TEST_SRS";
	
    protected DB23DTestSetup() {
        super(new DB2TestSetup());
        
    }

    @Override
    protected void createLine3DTable() throws Exception {
        //set up table
    	String tableName = "line3d";
    	String tableNameQuoted ="\""+tableName+"\"";
    	Connection con = getDataSource().getConnection();
        con.prepareStatement("CREATE TABLE "+DB2TestUtil.SCHEMA_QUOTED+"."+tableNameQuoted+"(\"fid\" int  generated always as identity (start with 0, increment by 1) , "
        	+ "\"id\" int,"	
        	+ "\"geom\" db2gse.st_linestring , "
            + " \"name\" varchar(255), PRIMARY KEY (\"fid\") )").execute();
        DB2Util.executeRegister(DB2TestUtil.SCHEMA, tableName, "geom", SRSNAME, con);
        // insert data
        con.prepareStatement("INSERT INTO "+DB2TestUtil.SCHEMA_QUOTED+"."+tableNameQuoted+"(\"id\",\"name\",\"geom\") VALUES ( 0,'l1', " +
        	"db2gse.ST_LineFromText('LINESTRING Z( 1 1 0, 2 2 0, 4 2 1, 5 1 1)',"+SRID+"))").execute();
        con.prepareStatement("INSERT INTO "+DB2TestUtil.SCHEMA_QUOTED+"."+tableNameQuoted+"(\"id\",\"name\",\"geom\") VALUES ( 1,'l2', " +
       		"db2gse.ST_LineFromText('LINESTRING Z( 3 0 1, 3 2 2, 3 3 3, 3 4 5)',"+SRID+"))").execute();
        con.close();
    	
    }

    @Override
    protected void createPoint3DTable() throws Exception {
      //set up table
    	String tableName = "point3d";
    	String tableNameQuoted ="\""+tableName+"\"";
    	Connection con = getDataSource().getConnection();
        con.prepareStatement("CREATE TABLE "+DB2TestUtil.SCHEMA_QUOTED+"."+tableNameQuoted+"(\"fid\" int  generated always as identity (start with 0, increment by 1) , "
        	+ "\"id\" int,"	
        	+ "\"geom\" db2gse.st_point , "
            + " \"name\" varchar(255), PRIMARY KEY (\"fid\") )").execute();
        DB2Util.executeRegister(DB2TestUtil.SCHEMA, tableName, "geom", SRSNAME, con);
        // insert data
        con.prepareStatement("INSERT INTO "+DB2TestUtil.SCHEMA_QUOTED+"."+tableNameQuoted+"(\"id\",\"name\",\"geom\") VALUES ( 0, 'p1'," +
        	"db2gse.ST_PointFromText('POINT Z(1 1 1)',"+SRID+"))").execute();
        con.prepareStatement("INSERT INTO "+DB2TestUtil.SCHEMA_QUOTED+"."+tableNameQuoted+"(\"id\",\"name\",\"geom\") VALUES ( 1, 'p2'," +
       		"db2gse.ST_PointFromText('POINT Z(3 0 1)',"+SRID+"))").execute();
        con.close();
    	
    }

    @Override
    protected void dropLine3DTable() throws Exception {
        Connection con = getDataSource().getConnection();
        DB2Util.executeUnRegister(DB2TestUtil.SCHEMA, "line3d", "geom", con);
    	DB2TestUtil.dropTable(DB2TestUtil.SCHEMA, "line3d", con);    	
        con.close();
    }

    @Override
    protected void dropPoly3DTable() throws Exception {
        Connection con = getDataSource().getConnection();
        DB2Util.executeUnRegister(DB2TestUtil.SCHEMA, "poly3d", "geom", con);
    	DB2TestUtil.dropTable(DB2TestUtil.SCHEMA, "line3d", con);    	
        con.close();
    	
    }

    @Override
    protected void dropPoint3DTable() throws Exception {
        Connection con = getDataSource().getConnection();
        DB2Util.executeUnRegister(DB2TestUtil.SCHEMA, "point3d", "geom", con);
    	DB2TestUtil.dropTable(DB2TestUtil.SCHEMA, "point3d", con);    	
        con.close();

    }

    
    

}
