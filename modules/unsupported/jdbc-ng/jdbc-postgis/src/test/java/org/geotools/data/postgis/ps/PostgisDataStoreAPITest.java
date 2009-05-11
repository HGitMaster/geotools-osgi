package org.geotools.data.postgis.ps;

import org.geotools.data.postgis.PostgisDataStoreAPITestSetup;
import org.geotools.jdbc.JDBCDataStoreAPITest;
import org.geotools.jdbc.JDBCDataStoreAPITestSetup;

public class PostgisDataStoreAPITest extends JDBCDataStoreAPITest {

    @Override
    protected JDBCDataStoreAPITestSetup createTestSetup() {
        return new PostgisDataStoreAPITestSetup(new PostGISPSTestSetup());
    }

    @Override
    public void testGetFeatureWriterConcurrency() throws Exception {
        // postgis will lock indefinitely, won't throw an exception
    }
}
