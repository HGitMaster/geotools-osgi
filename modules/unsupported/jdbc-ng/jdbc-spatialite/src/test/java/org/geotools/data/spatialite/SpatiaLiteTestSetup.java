package org.geotools.data.spatialite;

import java.sql.Connection;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCDataStoreFactory;
import org.geotools.jdbc.JDBCTestSetup;

public class SpatiaLiteTestSetup extends JDBCTestSetup {
    
    @Override
    protected JDBCDataStoreFactory createDataStoreFactory() {
        return new SpatiaLiteDataStoreFactory();
    }
    
    @Override
    protected void setUpDataStore(JDBCDataStore dataStore) {
        super.setUpDataStore(dataStore);
        dataStore.setDatabaseSchema(null);
    }
    
    @Override
    protected void initializeDataSource(BasicDataSource ds, Properties db) {
        super.initializeDataSource(ds, db);
        ds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
    }
    
    @Override
    protected void setUpData() throws Exception {
        //drop old data
        runSafe("DROP TABLE ft1");
        runSafe("DROP TABLE ft2");
        runSafe("DELETE FROM geometry_columns");
        
        //create some data
        String sql = "CREATE TABLE ft1 (id INTEGER PRIMARY KEY)";
        run( sql );
        
        sql = "SELECT AddGeometryColumn('ft1','geometry',4326,'POINT',2)";
        run( sql );
        
        sql = "ALTER TABLE ft1 add intProperty INTEGER";
        run( sql );
        
        sql = "ALTER TABLE ft1 add doubleProperty DOUBLE";
        run( sql );
        
        sql = "ALTER TABLE ft1 add stringProperty VARCHAR(255)";
        run( sql );

        sql = "INSERT INTO ft1 VALUES ("
            + "0,GeomFromText('POINT(0 0)',4326), 0, 0.0,'zero');";
        run(sql);

        sql = "INSERT INTO ft1 VALUES ("
            + "1,GeomFromText('POINT(1 1)',4326), 1, 1.1,'one');";
        run(sql);

        sql = "INSERT INTO ft1 VALUES ("
            + "2,GeomFromText('POINT(2 2)',4326), 2, 2.2,'two');";
        run(sql);
    }

}
