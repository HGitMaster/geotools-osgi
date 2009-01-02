package org.geotools.data.postgis.ps;

import org.geotools.jdbc.JDBCDataStoreTest;
import org.geotools.jdbc.JDBCTestSetup;

public class PostgisDataStoreTest extends JDBCDataStoreTest {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new PostGISPSTestSetup();
    }

}
