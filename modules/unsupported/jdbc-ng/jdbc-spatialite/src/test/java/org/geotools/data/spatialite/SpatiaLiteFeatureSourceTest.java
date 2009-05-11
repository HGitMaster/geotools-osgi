package org.geotools.data.spatialite;

import org.geotools.jdbc.JDBCFeatureSourceTest;
import org.geotools.jdbc.JDBCTestSetup;

public class SpatiaLiteFeatureSourceTest extends JDBCFeatureSourceTest {

    @Override
    protected JDBCTestSetup createTestSetup() {
        return new SpatiaLiteTestSetup();
    }

}
