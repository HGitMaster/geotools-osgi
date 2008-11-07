package org.geotools.data.db2;

import org.geotools.jdbc.JDBCPrimaryKeyTest;
import org.geotools.jdbc.JDBCPrimaryKeyTestSetup;

public class DB2PrimaryKeyTest extends JDBCPrimaryKeyTest {

    @Override
    protected JDBCPrimaryKeyTestSetup createTestSetup() {
        return new DB2PrimaryKeyTestSetup();
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        dataStore.setDatabaseSchema(DB2TestUtil.SCHEMA);
    }

}
