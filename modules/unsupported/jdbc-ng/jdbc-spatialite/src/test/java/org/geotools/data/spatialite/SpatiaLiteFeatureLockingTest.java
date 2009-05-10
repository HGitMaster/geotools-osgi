package org.geotools.data.spatialite;

import org.geotools.jdbc.JDBCFeatureLockingTest;
import org.geotools.jdbc.JDBCTestSetup;

public class SpatiaLiteFeatureLockingTest extends JDBCFeatureLockingTest {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new SpatiaLiteTestSetup();
    }

}
