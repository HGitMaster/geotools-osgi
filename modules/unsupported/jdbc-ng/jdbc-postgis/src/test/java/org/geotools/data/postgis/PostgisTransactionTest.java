package org.geotools.data.postgis;

import org.geotools.jdbc.JDBCTestSetup;
import org.geotools.jdbc.JDBCTransactionTest;

public class PostgisTransactionTest extends JDBCTransactionTest {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new PostGISTestSetup();
    }

}
