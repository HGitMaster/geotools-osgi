package org.geotools.data.postgis.ps;

import org.geotools.jdbc.JDBCFeatureLockingTest;
import org.geotools.jdbc.JDBCTestSetup;

public class PostgisFeatureLockingTest extends JDBCFeatureLockingTest {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new PostGISPSTestSetup();
    }

}
