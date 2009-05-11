package org.geotools.data.spatialite;

import org.geotools.jdbc.JDBCDataStoreTest;
import org.geotools.jdbc.JDBCTestSetup;

public class SpatiaLiteDataStoreTest extends JDBCDataStoreTest {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new SpatiaLiteTestSetup();
    }
    
    @Override
    public void testCreateSchemaWithConstraints() throws Exception {
        //SQLite does not enforce length restrictions on strings
        //See FAQ (9) from http://www.sqlite.org/faq.html 
    }

}
