package org.geotools.data.postgis;

import org.geotools.jdbc.JDBCDataStoreTest;
import org.geotools.jdbc.JDBCTestSetup;

public class PostgisDataStoreTest extends JDBCDataStoreTest {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new PostGISTestSetup();
    }

}
