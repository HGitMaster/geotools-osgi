package org.geotools.data.spatialite;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.jdbc.JDBCDataStoreAPITest;
import org.geotools.jdbc.JDBCDataStoreAPITestSetup;



public class SpatiaLiteDataStoreAPITest extends JDBCDataStoreAPITest {

    @Override
    protected JDBCDataStoreAPITestSetup createTestSetup() {
        return new SpatiaLiteDataStoreAPITestSetup();
    }
    
    @Override
    public void testTransactionIsolation() throws Exception {
        //super.testTransactionIsolation();
        //JD: In order to allow multiple connections from the same thread (which this test requires) 
        // we need to put the database in read_uncommitted mode, which means transaction isolation 
        // can not be achieved
    }

}
