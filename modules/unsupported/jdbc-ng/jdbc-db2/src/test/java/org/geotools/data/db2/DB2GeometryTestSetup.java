package org.geotools.data.db2;

import java.sql.Connection;
import java.sql.SQLException;

import org.geotools.jdbc.JDBCGeometryTestSetup;

public class DB2GeometryTestSetup extends JDBCGeometryTestSetup {

    protected DB2GeometryTestSetup() {
        super(new DB2TestSetup());

    }
    

    
    @Override
    protected void dropSpatialTable(String tableName) throws Exception {
    	Connection con = getDataSource().getConnection();
    	try {
    		DB2Util.executeUnRegister(DB2TestUtil.SCHEMA, tableName, "goem", con);
    		DB2TestUtil.dropTable(DB2TestUtil.SCHEMA, tableName,con);    		
    	} catch (SQLException e) {    		
    	}
    	
        con.close();
    }

}
