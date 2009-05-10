package org.geotools.data.spatialite;

import org.geotools.jdbc.JDBCBooleanTestSetup;

public class SpatiaLiteBooleanTestSetup extends JDBCBooleanTestSetup {

    protected SpatiaLiteBooleanTestSetup() {
        super(new SpatiaLiteTestSetup());
        
    }

    @Override
    protected void createBooleanTable() throws Exception {
        run( "CREATE TABLE b (fid INTEGER PRIMARY KEY, id INTEGER,boolProperty BOOLEAN)");
        run( "INSERT INTO b VALUES (0, 0, 0)");
        run( "INSERT INTO b VALUES (1, 1, 1)");
    }

    @Override
    protected void dropBooleanTable() throws Exception {
        run( "DROP TABLE b");
    }

}
