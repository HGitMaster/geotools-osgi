/*
 *    GeoTools - The Open Source Java GIS Toolkit
 
 */
package org.geotools.data.db2;

import java.sql.Connection;
import java.util.logging.Logger;



import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.JDBCTestSetup;


/**
 * Test harness for db2.
 *
 * @author Christian Mueller
 *
 */
public class DB2TestSetup extends JDBCTestSetup {
	
	protected final static Logger LOGGER = Logger.getLogger(DB2TestSetup.class.getPackage()
            .getName());
	

    
//    protected SQLDialect createSQLDialect(JDBCDataStore dataStore) {
//        return new DB2SQLDialect(dataStore);
//    }

    
    protected void setUpData() throws Exception {
    	
    	Connection con = getDataSource().getConnection();
    	
        //drop old data
    	
		DB2Util.executeUnRegister(DB2TestUtil.SCHEMA, "ft1", "geometry", con);    		    	
    	DB2TestUtil.dropTable(DB2TestUtil.SCHEMA, "ft1", con);
    	DB2Util.executeUnRegister(DB2TestUtil.SCHEMA, "ft2", "geometry", con);
    	DB2TestUtil.dropTable(DB2TestUtil.SCHEMA, "ft2", con);
    	

        //create some data
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE "+DB2TestUtil.SCHEMA_QUOTED+".\"ft1\" ").append("(\"id\" int  PRIMARY KEY not null , ")
          .append("\"geometry\" db2gse.ST_POINT, \"intProperty\" int, ")
          .append("\"doubleProperty\" double, \"stringProperty\" varchar(255))");
        con.prepareStatement(sb.toString()).execute();
        
        DB2Util.executeRegister(DB2TestUtil.SCHEMA, "ft1", "geometry", DB2TestUtil.SRSNAME, con);

        sb = new StringBuffer();
        sb.append("INSERT INTO "+DB2TestUtil.SCHEMA_QUOTED+".\"ft1\" VALUES (")
          .append("0,db2gse.st_PointFromText('POINT(0 0)',"+DB2TestUtil.SRID+"), 0, 0.0,'zero')");
        con.prepareStatement(sb.toString()).execute();

        sb = new StringBuffer();
        sb.append("INSERT INTO "+DB2TestUtil.SCHEMA_QUOTED+".\"ft1\" VALUES (")
          .append("1,db2gse.st_PointFromText('POINT(1 1)',"+DB2TestUtil.SRID+"), 1, 1.1,'one')");
        con.prepareStatement(sb.toString()).execute();

        sb = new StringBuffer();
        sb.append("INSERT INTO "+DB2TestUtil.SCHEMA_QUOTED+".\"ft1\" VALUES (")
          .append("2,db2gse.st_PointFromText('POINT(2 2)',"+DB2TestUtil.SRID+"), 2, 2.2,'two')");
        con.prepareStatement(sb.toString()).execute();
        
        con.close();
    }



	@Override
	protected void initializeDatabase() throws Exception {
		super.initializeDatabase();
		Connection con = getDataSource().getConnection();
		DB2TestUtil.enableDB(con);
		con.close();
	}



	@Override
	protected JDBCDataStoreFactory createDataStoreFactory() {
		return new DB2NGDataStoreFactory();
	}
}
